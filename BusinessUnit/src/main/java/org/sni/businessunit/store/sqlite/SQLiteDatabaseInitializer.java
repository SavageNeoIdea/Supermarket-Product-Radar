package org.sni.businessunit.store.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseInitializer {

    private final SQLiteConnection connection;
    private final String createTableSQL = """
                                            CREATE TABLE IF NOT EXISTS product (
                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                name TEXT NOT NULL,
                                                price REAL NOT NULL,
                                                measure TEXT,
                                                quantity INTEGER,
                                                packageQuantity INTEGER,
                                                ean TEXT NOT NULL,
                                                brand TEXT,
                                                source TEXT NOT NULL,
                                                ts TEXT,
                                                embedding_vector TEXT,
                                                UNIQUE(ean, source)
                                            );
                                        """;

    public SQLiteDatabaseInitializer(SQLiteConnection connection){
        this.connection = connection;
    }
    public void init() {
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing SQLite database", e);
        }
    }
}