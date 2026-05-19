package store.sqlite;
import store.DatamartConnection;
import store.activemq.ConfigReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection implements DatamartConnection {
    private static final String DB_URL = new ConfigReader().loadConfig("subscribers", "businessUnitSubscriber").get("datamartUrl");
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}