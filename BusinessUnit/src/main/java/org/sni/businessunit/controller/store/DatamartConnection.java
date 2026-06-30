package org.sni.businessunit.controller.store;
import java.sql.Connection;
import java.sql.SQLException;

public interface DatamartConnection {
    Connection getConnection() throws SQLException;
}
