package org.sni.spr.store;

import org.sni.spr.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SQLiteStorer implements Storer {

    private static final String URL = "jdbc:sqlite:products.db";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS products (
                id REAL NOT NULL,
                timestamp TEXT,
                ean TEXT,
                brand TEXT,
                category TEXT,
                subcategory TEXT,
                name TEXT,
                packageQty INTEGER,
                qty REAL,
                measure TEXT,
                price REAL,
                gluten BOOLEAN,
                urlImage TEXT,
                PRIMARY KEY (id, timestamp)
            )
        """;
        try (Connection conn = connect();
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    @Override
    public void saveAll(List<Product> products) {
        init();
        String sql = """
            INSERT INTO products
            (id, timestamp, ean, brand, category, subcategory, name, packageQty, qty, measure, price, gluten, urlImage)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Product p : products) {
                ps.setFloat(1, p.getId());
                ps.setString(2, timestamp);
                ps.setString(3, p.getEan());
                ps.setString(4, p.getBrand());
                ps.setString(5, p.getCategory());
                ps.setString(6, p.getSubcategory());
                ps.setString(7, p.getDisplayName());
                ps.setInt(8, p.getTotalUnits());
                ps.setDouble(9, p.getUnitSize());
                ps.setString(10, p.getUnitType());
                ps.setDouble(11, p.getUnitPrice());
                ps.setBoolean(12, p.getGluten());
                ps.setString(13, p.getThumbnail());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Error saving products", e);
        }
    }

    @Override
    public void close(){}
}