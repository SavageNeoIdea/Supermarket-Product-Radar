package org.sni.businessunit.store.sqlite;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.sni.businessunit.store.SemanticEngine;
import org.sni.businessunit.store.SearchQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class SQLiteQuery implements SearchQuery {

    private static final double SIMILARITY_THRESHOLD = 0.45;
    private static final int MAX_RESULTS = 15;

    private final SQLiteConnection sqLiteConnection;
    private final SemanticEngine semanticEngine;
    private final Gson gson;

    public SQLiteQuery(SQLiteConnection sqLiteConnection, SemanticEngine semanticEngine) {
        this.sqLiteConnection = sqLiteConnection;
        this.semanticEngine = semanticEngine;
        this.gson = new Gson();
    }

    @Override
    public Map<String, Map<String, List<String>>> searchQuery(String input) {
        if (isInvalidInput(input)) {
            return emptySearchResponse();
        }
        List<ScoredProduct> topMatches = findTopMatchingProducts(input);
        return formatAsSearchResponse(input, topMatches);
    }

    private boolean isInvalidInput(String input) {
        return input == null || input.isBlank();
    }

    private Map<String, Map<String, List<String>>> emptySearchResponse() {
        Map<String, Map<String, List<String>>> emptyResult = new LinkedHashMap<>();
        emptyResult.put("", new HashMap<>());
        return emptyResult;
    }

    private List<ScoredProduct> findTopMatchingProducts(String input) {
        float[] queryVector = semanticEngine.embed(input);
        List<RowData> catalog = fetchAllProductsFromDatabase();

        return rankAndFilterTopProducts(catalog, queryVector);
    }

    private Map<String, Map<String, List<String>>> formatAsSearchResponse(String input, List<ScoredProduct> topMatches) {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        result.put(input, groupResultsBySource(topMatches));
        return result;
    }

    private List<RowData> fetchAllProductsFromDatabase() {
        String sql = "SELECT source, name, price, measure, quantity, packageQuantity, brand, embedding_vector FROM product";
        return readAllRows(sql);
    }

    private List<RowData> readAllRows(String sqlQuery) {
        List<RowData> rows = new ArrayList<>();
        try (Connection conn = sqLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String embeddingStr = rs.getString("embedding_vector");
                if (embeddingStr != null && !embeddingStr.isBlank()) {
                    rows.add(new RowData(
                            rs.getString("source"), rs.getString("name"),
                            rs.getDouble("price"), rs.getString("measure"),
                            rs.getInt("quantity"), rs.getInt("packageQuantity"),
                            rs.getString("brand"), embeddingStr
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la lectura en la base de datos", e);
        }
        return rows;
    }

    private List<ScoredProduct> rankAndFilterTopProducts(List<RowData> catalog, float[] queryVector) {
        return catalog.parallelStream()
                .map(row -> scoreProduct(row, queryVector))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
                .limit(MAX_RESULTS)
                .toList();
    }

    private ScoredProduct scoreProduct(RowData row, float[] queryVector) {
        try {
            float[] productVector = gson.fromJson(row.embeddingJson, float[].class);
            double similarity = semanticEngine.computeRelevance(queryVector, productVector);

            if (similarity > SIMILARITY_THRESHOLD) {
                return new ScoredProduct(row, similarity);
            }
        } catch (Exception e) {
            System.err.printf("Error calculando similitud para producto [%s]: %s%n", row.name, e.getMessage());
        }
        return null;
    }

    private Map<String, List<String>> groupResultsBySource(List<ScoredProduct> candidatos) {
        Map<String, List<String>> sourceEventMap = new HashMap<>();
        for (ScoredProduct sp : candidatos) {
            String jsonEvent = buildJsonEvent(sp.row, sp.score);
            sourceEventMap.computeIfAbsent(sp.row.source, k -> new ArrayList<>()).add(jsonEvent);
        }
        return sourceEventMap;
    }

    private String buildJsonEvent(RowData row, double similitud) {
        JsonObject root = new JsonObject();
        root.addProperty("uid", UUID.randomUUID().toString());
        root.addProperty("ts", Instant.now().toString());
        root.addProperty("ss", row.source);

        JsonObject payload = new JsonObject();
        payload.addProperty(row.source + "Name", row.name);
        payload.addProperty(row.source + "Price", row.price);
        payload.addProperty(row.source + "Measure", row.measure);
        payload.addProperty(row.source + "Qty", row.quantity);
        payload.addProperty(row.source + "PackageQty", row.packageQuantity);
        payload.addProperty(row.source + "Brand", row.brand);
        payload.addProperty("SimilarityScore", similitud);

        root.add("payload", payload);
        return root.toString();
    }

    private static class RowData {
        final String source, name, measure, brand, embeddingJson;
        final double price;
        final int quantity, packageQuantity;

        RowData(String source, String name, double price, String measure, int quantity, int packageQuantity, String brand, String embeddingJson) {
            this.source = source; this.name = name; this.price = price;
            this.measure = measure; this.quantity = quantity;
            this.packageQuantity = packageQuantity; this.brand = brand;
            this.embeddingJson = embeddingJson;
        }
    }

    private static class ScoredProduct {
        final RowData row;
        final double score;

        ScoredProduct(RowData row, double score) {
            this.row = row;
            this.score = score;
        }

        public double getScore() {
            return score;
        }
    }
}