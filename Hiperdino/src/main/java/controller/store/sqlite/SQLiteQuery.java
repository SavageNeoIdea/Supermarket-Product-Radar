package controller.store.sqlite;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class SQLiteQuery {

    public static  Map<String, Map<String, List<String>>> searchQuery(String input) {
        Map<String, Map<String, List<String>>> data = new LinkedHashMap<>();
        Map<String, List<String>> sourceEventMap = new HashMap<>();

        if (input == null || input.trim().isEmpty()) {
            System.out.println("El término de búsqueda está vacío.");
            data.put("", sourceEventMap);
            return data;
        }

        String[] tokens = extractTokens(input);
        String sqlQuery = buildQuery(tokens);

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < tokens.length; i++) {
                pstmt.setString(i + 1, "%" + tokens[i] + "%");
            }

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

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la búsqueda en la base de datos", e);
        }

        data.put(input, sourceEventMap);
        return data;
    }

    private static String[] extractTokens(String input) {
        return input.trim().split("\\s+");
    }

    private static String buildQuery(String[] tokens) {
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