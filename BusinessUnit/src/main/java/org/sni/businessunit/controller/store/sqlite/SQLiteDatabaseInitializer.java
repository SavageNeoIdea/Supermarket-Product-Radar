package org.sni.businessunit.controller.store.sqlite;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseInitializer {

    private final SQLiteConnection connection;

    public SQLiteDatabaseInitializer(SQLiteConnection connection) {
        this.connection = connection;
    }

    public void init() {
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                        CREATE TABLE IF NOT EXISTS product (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            price REAL NOT NULL,
                            measure TEXT NOT NULL,
                            quantity REAL NOT NULL,
                            packageQuantity INTEGER NOT NULL,
                            ean TEXT NOT NULL,
                            brand TEXT NOT NULL,
                            source TEXT NOT NULL,
                            ts TEXT NOT NULL,
                            embedding_vector TEXT,
                            UNIQUE(ean, source)
                        );
                    """;
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing SQLite database", e);
        }
    }
}