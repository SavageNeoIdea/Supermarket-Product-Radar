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
        String sql = """
            INSERT INTO product
            (name, price, measure, quantity, packageQuantity, ean, brand, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Product product : products) {

                stmt.setString(1, product.getName());
                stmt.setDouble(2, product.getPrice());
                stmt.setString(3, product.getMeasure());
                stmt.setInt(4, product.getQuantity());
                stmt.setInt(5, product.getPackageQuantity());
                stmt.setString(6, product.getEan());
                stmt.setObject(7, product.getBrand());
                stmt.setObject(8, product.getSource());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error storing products", e);
        }
    }
}