package org.sni.spr.mercadona.controller.store;

import org.sni.spr.mercadona.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SQLiteStorer implements Storer {
    private static final String URL = "jdbc:sqlite:products.db";
    private final Connection connection;

    public SQLiteStorer() {
        try {
            this.connection = DriverManager.getConnection(URL);
            init();
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed", e);
        }
    }

    private void init() {
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
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    @Override
    public void save(Product p) {
        String sql = """
                    INSERT INTO products
                    (id, timestamp, ean, brand, category, subcategory, name, packageQty, qty, measure, price, gluten, urlImage)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
            ps.executeUpdate();
            System.out.println("[SQLITE] Saved product: " + p.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product " + p.getId(), e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error closing DB connection", e);
        }
    }
}