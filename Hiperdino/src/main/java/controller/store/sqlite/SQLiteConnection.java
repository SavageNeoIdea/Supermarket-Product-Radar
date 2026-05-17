package controller.store.sqlite;
import controller.store.DatamartConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection implements DatamartConnection {

    private static final String DB_URL = "jdbc:sqlite:datamart.db";

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}