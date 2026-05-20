package org.sni.spr.hiperdino.store;

import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.sql.*;
import java.util.List;

public class HiperdinoSqlStore implements Store {
    private static final String DB_URL = "jdbc:sqlite:myDB.db";
    private Connection connection;
    private PreparedStatement insertStatement;
    private String createTableSql = """
                CREATE TABLE IF NOT EXISTS products (
                    sku TEXT,
                    timestamp TEXT,
                    uid TEXT,
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

    public HiperdinoSqlStore() {
        initDatabase();
        openConnection();
    }

    private void initDatabase() {

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute(createTableSql);
            System.out.println("Base de datos SQLite inicializada correctamente.");
        } catch (SQLException e) {
            System.err.println("Error inicializando DB: " + e.getMessage());
        }
    }

    private void openConnection() {
        String insertSql = """
                    INSERT OR IGNORE INTO products (
                        sku, timestamp, uid, ean, brand, category, subcategory,
                        name, qty, package_qty, measure, price, gluten, image_url
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            connection = DriverManager.getConnection(DB_URL);
            insertStatement = connection.prepareStatement(insertSql);
        } catch (SQLException e) {
            System.err.println("Error abriendo conexión persistente: " + e.getMessage());
        }
    }

    @Override
    public synchronized void storeSingleData(HiperdinoProduct product) {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
            fillPreparedStatement(insertStatement, product);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando producto " + product.getHiperdinoEventId() + ": " + e.getMessage());
        }
    }

    @Override
    public synchronized void storeAllData(List<HiperdinoProduct> productList) {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
            connection.setAutoCommit(false);
            for (HiperdinoProduct product : productList) {
                fillPreparedStatement(insertStatement, product);
                insertStatement.addBatch();
            }
            int[] results = insertStatement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Insertados " + results.length + " productos en SQLite (Batch).");
        } catch (SQLException e) {
            System.err.println("Error en el batch de base de datos: " + e.getMessage());
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            if (insertStatement != null) insertStatement.close();
            if (connection != null) connection.close();
            System.out.println("Conexión a SQLite cerrada.");
        } catch (SQLException e) {
            System.err.println("Error cerrando SQLite: " + e.getMessage());
        }
    }


    private void fillPreparedStatement(PreparedStatement stmt, HiperdinoProduct product) throws SQLException {
        stmt.setString(1, product.getHiperdinoSku());
        stmt.setString(2, product.getHiperdinoTs().toString());
        stmt.setString(3, product.getHiperdinoEventId().toString());
        stmt.setString(4, product.getHiperdinoEan());
        stmt.setString(5, product.getHiperdinoBrand());
        stmt.setString(6, product.getHiperdinoCategory());
        stmt.setString(7, product.getHiperdinoSubcategory());
        stmt.setString(8, product.getHiperdinoName());
        stmt.setInt(9, product.getHiperdinoQty());
        stmt.setInt(10, product.getHiperdinoPackageQty());
        stmt.setString(11, product.getHiperdinoMeasure());
        stmt.setDouble(12, product.getHiperdinoPrice());
        stmt.setInt(13, product.getHiperdinoGluten() ? 1 : 0);
        stmt.setString(14, product.getHiperdinoUrlImage());
    }
}