package controller.store.sqlite;
import com.google.gson.JsonObject;
import controller.store.SearchQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class SQLiteQuery implements SearchQuery {

    private final SQLiteConnection sqLiteConnection;
    private Map<String, List<String>> sourceEventMap;

    public SQLiteQuery(SQLiteConnection sqLiteConnection) {
        this.sqLiteConnection = sqLiteConnection;
    }

    public  Map<String, Map<String, List<String>>> searchQuery(String input) {
        Map<String, Map<String, List<String>>> data = new LinkedHashMap<>();
        sourceEventMap = new HashMap<>();
        if (haveDataInside(input, data)) { data.put("", sourceEventMap); return data;}
        String[] tokens = extractTokens(input);
        String sqlQuery = buildQuery(tokens);

        try (Connection conn = sqLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            for (int i = 0; i < tokens.length; i++) {
                pstmt.setString(i + 1, "%" + tokens[i] + "%");
            }
            getSourceEventMap(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la búsqueda en la base de datos", e);
        }
        data.put(input, sourceEventMap);
        return data;
    }

    private boolean haveDataInside(String input, Map<String, Map<String, List<String>>> data) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("El término de búsqueda está vacío.");
            return true;
        }
        return false;
    }

    private void getSourceEventMap(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String source = rs.getString("source");
                JsonObject root = new JsonObject();
                root.addProperty("uid", UUID.randomUUID().toString());
                root.addProperty("ts", Instant.now().toString());
                root.addProperty("ss", source);
                JsonObject payload = new JsonObject();
                payload.addProperty(source + "Name", rs.getString("name"));
                payload.addProperty(source + "Price", rs.getDouble("price"));
                payload.addProperty(source + "Measure", rs.getString("measure"));
                payload.addProperty(source + "Qty", rs.getInt("quantity"));
                payload.addProperty(source + "PackageQty", rs.getInt("packageQuantity"));
                payload.addProperty(source + "Brand", rs.getString("brand"));
                root.add("payload", payload);
                sourceEventMap.computeIfAbsent(source, key -> new ArrayList<>())
                        .add(root.toString());
            }
        }
    }

    private String[] extractTokens(String input) {
        return input.trim().split("\\s+");
    }

    private String buildQuery(String[] tokens) {
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT DISTINCT source, name, price, measure, quantity, packageQuantity, brand FROM product WHERE "
        );
        for (int i = 0; i < tokens.length; i++) {
            queryBuilder.append("name LIKE ?");
            if (i < tokens.length - 1) {
                queryBuilder.append(" AND ");
            }
        }
        return queryBuilder.toString();
    }
}