package controller.store.sqlite;

import controller.store.DatamartConnection;
import controller.store.DatamartStore;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SqLiteDatamartStore implements DatamartStore {

    private final SQLiteConnection sqLiteConnection;
    private final SQLiteDatabaseInitializer sqLiteDatabaseInitializer;

    public final String sql ="""
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

    public SqLiteDatamartStore(SQLiteConnection sqLiteConnection) {
        this.sqLiteConnection = sqLiteConnection;
        this.sqLiteDatabaseInitializer = new SQLiteDatabaseInitializer(sqLiteConnection);
        sqLiteDatabaseInitializer.init();
    }
    
    @Override
    public void storeAllData(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        List<Product> validProducts = getValidProducts(products);
        if (validProducts.isEmpty()) return;
        try (Connection conn = sqLiteConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                addStatement(validProducts, stmt);
                conn.commit();
            } catch (SQLException e) {conn.rollback(); throw e;}
        } catch (SQLException e) {throw new RuntimeException("Error storing products en SQLite", e);}
    }

    private static void addStatement(List<Product> validProducts, PreparedStatement stmt) throws SQLException {
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
    }

    private List<Product> getValidProducts(List<Product> products) {
        return products.stream()
                .filter(p -> p.getEan() != null && !p.getEan().trim().isEmpty())
                .toList();
    }
}