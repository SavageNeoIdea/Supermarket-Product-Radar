package org.sni.businessunit.controller.shoppinglist;

import org.sni.businessunit.model.Product;
import org.sni.businessunit.store.sqlite.SQLiteConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ProductCharger {
    private final SQLiteConnection sqLiteConnection;

    public ProductCharger(SQLiteConnection sqLiteConnection) {
        this.sqLiteConnection = sqLiteConnection;
    }

    public List<Product> fetchAllProductsFromDatabase() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT source, name, price, measure, quantity, packageQuantity, ean, brand, ts, embedding_vector FROM product";
        try (Connection conn = sqLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String embeddingStr = rs.getString("embedding_vector");
                if (productHaveEmbedding(embeddingStr)) {
                    products.add(new Product(
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("measure"),
                            rs.getInt("quantity"),
                            rs.getInt("packageQuantity"),
                            rs.getString("ean"),
                            rs.getString("brand"),
                            rs.getString("source"),
                            rs.getString("ts"),
                            embeddingStr
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la lectura en la base de datos", e);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Error en los datos de la base de datos (posible fallo en enum o campo nulo)", e);
        }
        return products;
    }

    private static boolean productHaveEmbedding(String embeddingStr) {
        return embeddingStr != null && !embeddingStr.isBlank();
    }
}
