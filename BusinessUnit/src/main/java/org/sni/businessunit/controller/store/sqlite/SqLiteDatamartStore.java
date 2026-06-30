package org.sni.businessunit.controller.store.sqlite;
import org.sni.businessunit.model.Product;
import org.sni.businessunit.controller.store.DatamartStore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SqLiteDatamartStore implements DatamartStore {

    private final SQLiteConnection sqLiteConnection;

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

    public SqLiteDatamartStore(SQLiteConnection sqLiteConnection) {
        this.sqLiteConnection = sqLiteConnection;
        new SQLiteDatabaseInitializer(sqLiteConnection).init();
    }

    @Override
    public void storeAllData(List<Product> products) {
        if (productListIsNotValid(products)) return;
        persistProductsList(products);
    }

    private boolean productListIsNotValid(List<Product> products) {
        return products == null || products.isEmpty();
    }

    private void persistProductsList(List<Product> products) {
        try (Connection conn = sqLiteConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                prepareStatements(products, stmt);
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error almacenando productos en SQLite", e);
        }
    }

    private void prepareStatements(List<Product> products, PreparedStatement stmt) throws SQLException {
        for (Product product : products) {
            storeSingleData(product, stmt);
        }
    }

    private void storeSingleData(Product product, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, product.getName());
        stmt.setDouble(2, product.getPrice());
        stmt.setString(3, product.getMeasure().toString());
        stmt.setDouble(4, product.getQuantity());
        stmt.setInt(5, product.getPackageQuantity());
        stmt.setString(6, product.getEan());
        stmt.setString(7, product.getBrand());
        stmt.setString(8, product.getSource());
        stmt.setString(9, product.getTs());
        stmt.setString(10, product.getEmbeddingVector());
        stmt.addBatch();
    }
}