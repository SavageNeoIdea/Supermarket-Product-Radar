package controller.store.sqlite;

import com.google.gson.Gson;
import controller.store.DatamartStore;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SqLiteDatamartStore implements DatamartStore {

    private final SQLiteConnection sqLiteConnection;
    private final SQLiteDatabaseInitializer sqLiteDatabaseInitializer;
    private final IAService iaService;
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

    public SqLiteDatamartStore(SQLiteConnection sqLiteConnection, IAService iaService) {
        this.sqLiteConnection = sqLiteConnection;
        this.iaService = iaService;
        this.gson = new Gson();
        this.sqLiteDatabaseInitializer = new SQLiteDatabaseInitializer(sqLiteConnection);
        sqLiteDatabaseInitializer.init();
    }

    @Override
    public void storeAllData(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        List<Product> validProducts = getValidProducts(products); //QUITAR
        if (validProducts.isEmpty()) return;
        System.out.println("Iniciando cálculo vectorial multihilo para " + validProducts.size() + " productos...");
        long startTime = System.currentTimeMillis();
        int numNucleos = Runtime.getRuntime().availableProcessors();
        ExecutorService embeddingExecutor = Executors.newFixedThreadPool(numNucleos);
        for (Product product : validProducts) {
            embeddingExecutor.submit(() -> {
                try {
                    product.generateEmbedding(iaService, gson);
                } catch (Exception e) {
                    System.err.println("Error calculando embedding para: " + product.getName() + " - " + e.getMessage());
                }
            });
        }
        embeddingExecutor.shutdown();
        try {
            if (!embeddingExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("¡Alerta! El tiempo de espera para calcular embeddings expiró.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("El entrenamiento inicial del Datamart fue interrumpido", e);
        }

        long embeddingTime = System.currentTimeMillis();
        System.out.println("¡IA satisfecha! Tiempo de cálculo: " + (embeddingTime - startTime) + "ms. Guardando en SQLite...");
        try (Connection conn = sqLiteConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Product product : validProducts) {
                    storeSingleData(product, stmt);
                }
                stmt.executeBatch();
                conn.commit();
                long endTime = System.currentTimeMillis();
                System.out.println("¡Lote persistido con éxito! Tiempo de inserción: " + (endTime - embeddingTime) + "ms. Tiempo total: " + (endTime - startTime) + "ms.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error storing products en SQLite", e);
        }
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

    private List<Product> getValidProducts(List<Product> products) {
        return products.stream()
                .filter(p -> p.getEan() != null && !p.getEan().trim().isEmpty())
                .toList();
    }
}