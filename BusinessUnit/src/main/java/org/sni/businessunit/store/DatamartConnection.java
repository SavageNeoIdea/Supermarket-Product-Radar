package org.sni.businessunit.store;

import java.sql.SQLException;

public interface DatamartConnection {
    public java.sql.Connection getConnection() throws SQLException;
}
