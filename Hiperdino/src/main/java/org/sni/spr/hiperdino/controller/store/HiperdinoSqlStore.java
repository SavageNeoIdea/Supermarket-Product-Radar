package org.sni.spr.hiperdino.controller.store;

import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class HiperdinoSqlStore implements Store {
    private static final String DB_URL = "jdbc:sqlite:myDB.db";
    public HiperdinoSqlStore() {
        initDatabase();
    }

    private void initDatabase() {
        String createTableSql = """
            
                CREATE TABLE IF NOT EXISTS products (
                sku TEXT,
                timestamp TEXT,
                ean TEXT,
                brand TEXT,
                category TEXT,
                subcategory TEXT,
                name TEXT,
                qty INTEGER,
                package_qty INTEGER,
                measure TEXT,
                price REAL,
                gluten INTEGER,
                image_url TEXT,
                PRIMARY KEY (sku, timestamp)
                            );
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        } catch (SQLException e) {
            System.err.println("Error initializing DB: " + e.getMessage());
        }
    }

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        String insertSql =
                """
        INSERT OR IGNORE INTO products (
            sku, timestamp, ean, sap_id, brand
                , category,
            subcategory,
                name, qty, package_qty,
                        measure, price, gluten, image_url
        ) VALUES (?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);

            for (HiperdinoProduct product : productList) {

                statement.setString(1, product.getHiperdinoSku());
                statement.setString(2, product.getHiperdinoTs().toString());
                statement.setString(3, product.getHiperdinoEan());
                statement.setString(4, product.getHiperdinoEventId().toString());
                statement.setString(5, product.getHiperdinoBrand());
                statement.setString(6, product.getHiperdinoCategory());
                statement.setString(7, product.getHiperdinoSubcategory());
                statement.setString(8, product.getHiperdinoName());
                statement.setInt(9, product.getHiperdinoQty());
                statement.setInt(10, product.getHiperdinoPackageQty());
                statement.setString(11, product.getHiperdinoMeasure());
                statement.setDouble(12, product.getHiperdinoPrice());
                statement.setInt(13, product.getHiperdinoGluten() ? 1 : 0);
                statement.setString(14, product.getHiperdinoUrlImage());

                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            conn.commit();
            System.out.println("Insertados " + results.length + " productos.");

        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
        }
    }
}