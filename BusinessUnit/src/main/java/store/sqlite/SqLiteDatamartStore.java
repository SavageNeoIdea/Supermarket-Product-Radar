package store.sqlite;

import com.google.gson.Gson;
import store.DatamartStore;
import model.Product;
import store.EmbeddingService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SqLiteDatamartStore implements DatamartStore {

    private final SQLiteConnection sqLiteConnection;
    private final SQLiteDatabaseInitializer sqLiteDatabaseInitializer;
    private final EmbeddingService iaService;
    private final Gson gson;
    public final String sql = """
                                INSERT INTO product 
                                (name, price, measure, quantity, packageQuantity, ean, brand, source, ts, embedding_vector)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                ON CONFLICT(ean, source) DO UPDATE SET
                                    name = EXCLUDED.name,
                                    price = EXCLUDED.price,
                                    measure = EXCLUDED.measure,
                                    quantity = EXCLUDED.quantity,
                                    packageQuantity = EXCLUDED.packageQuantity,
                                    brand = EXCLUDED.brand,
                                    ts = EXCLUDED.ts,
                                    embedding_vector = EXCLUDED.embedding_vector
                                WHERE EXCLUDED.ts > product.ts;
                            """;

    public SqLiteDatamartStore(SQLiteConnection sqLiteConnection, EmbeddingService iaService) {
        this.sqLiteConnection = sqLiteConnection;
        this.iaService = iaService;
        this.gson = new Gson();
        this.sqLiteDatabaseInitializer = new SQLiteDatabaseInitializer(sqLiteConnection);
        sqLiteDatabaseInitializer.init();
    }

    @Override
    public void storeAllData(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        long startTime = System.currentTimeMillis();
        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        try {
            computeEmbeddingsConcurrently(products, executor);
            long afterEmbeddings = System.currentTimeMillis();
            persistProductsBatch(products);
            long endTime = System.currentTimeMillis();
        } finally {
            if (!executor.isShutdown()) executor.shutdownNow();
        }
    }

    private void computeEmbeddingsConcurrently(List<Product> products, ExecutorService executor) {
        for (Product product : products) {
            executor.submit(() -> {
                try {
                    product.generateEmbedding(iaService, gson);
                } catch (Exception e) {
                    logError("Error calculando embedding para: " + product.getName() + " - " + e.getMessage());
                }
            });
        }
        awaitExecutorTermination(executor, 5, TimeUnit.MINUTES);
    }

    private void awaitExecutorTermination(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                logError("¡Alerta! El tiempo de espera para calcular embeddings expiró.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("El entrenamiento inicial del Datamart fue interrumpido", e);
        }
    }

    private void persistProductsBatch(List<Product> products) {
        try (Connection conn = sqLiteConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Product product : products) {
                    storeSingleData(product, stmt);
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error storing products en SQLite", e);
        }
    }

    private void logInfo(String msg) {
        System.out.println(msg);
    }
    private void logError(String msg) {
        System.err.println(msg);
    }

    private void storeSingleData(Product product, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, product.getName());
        stmt.setDouble(2, product.getPrice());
        stmt.setString(3, product.getMeasure().toString());
        stmt.setInt(4, product.getQuantity());
        stmt.setInt(5, product.getPackageQuantity());
        stmt.setString(6, product.getEan());
        stmt.setString(7, product.getBrand());
        stmt.setString(8, product.getSource());
        stmt.setString(9, product.getTs());
        stmt.setString(10, product.getEmbeddingVector());
        stmt.addBatch();
    }
}