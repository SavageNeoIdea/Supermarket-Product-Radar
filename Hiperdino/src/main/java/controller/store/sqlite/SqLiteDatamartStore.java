package controller.store.sqlite;

import controller.store.DatamartStore;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SqLiteDatamartStore implements DatamartStore {

    public SqLiteDatamartStore() {
        SQLiteDatabaseInitializer.init();
    }

    @Override
    public void storeAllData(List<Product> products) {
        if (products == null || products.isEmpty()) return;

        // Filtramos para procesar solo productos que tengan un EAN válido
        List<Product> validProducts = products.stream()
                .filter(p -> p.getEan() != null && !p.getEan().trim().isEmpty())
                .toList();

        if (validProducts.isEmpty()) return;

        String sql = """
        INSERT INTO product 
        (name, price, measure, quantity, packageQuantity, ean, brand, source, ts)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(ean, source) DO UPDATE SET
            name = EXCLUDED.name,
            price = EXCLUDED.price,
            measure = EXCLUDED.measure,
            quantity = EXCLUDED.quantity,
            packageQuantity = EXCLUDED.packageQuantity,
            brand = EXCLUDED.brand,
            ts = EXCLUDED.ts
        WHERE EXCLUDED.ts > product.ts;
    """;

        try (Connection conn = SQLiteConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Product product : validProducts) {
                    stmt.setString(1, product.getName());
                    stmt.setDouble(2, product.getPrice());
                    stmt.setString(3, product.getMeasure().toString());
                    stmt.setInt(4, product.getQuantity());
                    stmt.setInt(5, product.getPackageQuantity());
                    stmt.setString(6, product.getEan());
                    stmt.setString(7, product.getBrand());
                    stmt.setString(8, product.getSource());
                    stmt.setString(9, product.getTs());

                    stmt.addBatch();
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
}