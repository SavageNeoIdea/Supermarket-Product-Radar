package controller.store.sqlite;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import controller.store.SearchQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SQLiteQuery implements SearchQuery {

    private final SQLiteConnection sqLiteConnection;
    private final IAService iaService;
    private final Gson gson;
    private final ExecutorService executorService;

    public SQLiteQuery(SQLiteConnection sqLiteConnection, IAService iaService) {
        this.sqLiteConnection = sqLiteConnection;
        this.iaService = iaService;
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Map<String, Map<String, List<String>>> searchQuery(String input) {
        Map<String, Map<String, List<String>>> data = new LinkedHashMap<>();
        Map<String, List<String>> sourceEventMap = new HashMap<>();
        if (haveDataInside(input, data)) {
            data.put("", sourceEventMap);
            return data;
        }
        float[] vectorEntrada = iaService.obtainVector(input);
        String sqlQuery = "SELECT source, name, price, measure, quantity, packageQuantity, brand, embedding_vector FROM product";
        executeVectorialDatabaseQuery(sqlQuery, vectorEntrada, sourceEventMap);
        data.put(input, sourceEventMap);
        return data;
    }

    private void executeVectorialDatabaseQuery(String sqlQuery, float[] vectorEntrada, Map<String, List<String>> sourceEventMap) {
        List<ScoredProduct> candidatos = Collections.synchronizedList(new ArrayList<>());
        ExecutorService queryExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (Connection conn = sqLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String embeddingStr = rs.getString("embedding_vector");
                if (embeddingStr == null || embeddingStr.isBlank()) continue;
                String source = rs.getString("source");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String measure = rs.getString("measure");
                int quantity = rs.getInt("quantity");
                int packageQuantity = rs.getInt("packageQuantity");
                String brand = rs.getString("brand");
                queryExecutor.submit(() -> {
                    float[] vectorProducto = gson.fromJson(embeddingStr, float[].class);
                    double similitud = iaService.calculateSimilitude(vectorEntrada, vectorProducto);
                    if (similitud > 0.45) {
                        String jsonEvent = buildJsonEvent(source, name, price, measure, quantity, packageQuantity, brand, similitud);
                        candidatos.add(new ScoredProduct(source, similitud, jsonEvent, sourceEventMap));
                    }
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la lectura en la base de datos", e);
        }
        queryExecutor.shutdown();
        try {
            if (!queryExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("La búsqueda vectorial tardó demasiado y fue forzada a detenerse.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("El proceso de búsqueda multihilo fue interrumpido", e);
        }
        candidatos.sort((a, b) -> Double.compare(b.similitud, a.similitud));
        int limite = Math.min(15, candidatos.size());
        for (int i = 0; i < limite; i++) {
            candidatos.get(i).addToMap();
        }
    }

    private boolean haveDataInside(String input, Map<String, Map<String, List<String>>> data) {
        return input == null || input.trim().isEmpty();
    }

    private String buildJsonEvent(String source, String name, double price, String measure, int quantity, int packageQuantity, String brand, double similitud) {
        JsonObject root = new JsonObject();
        root.addProperty("uid", UUID.randomUUID().toString());
        root.addProperty("ts", Instant.now().toString());
        root.addProperty("ss", source);
        JsonObject payload = new JsonObject();
        payload.addProperty(source + "Name", name);
        payload.addProperty(source + "Price", price);
        payload.addProperty(source + "Measure", measure);
        payload.addProperty(source + "Qty", quantity);
        payload.addProperty(source + "PackageQty", packageQuantity);
        payload.addProperty(source + "Brand", brand);
        payload.addProperty("SimilarityScore", similitud);
        root.add("payload", payload);
        return root.toString();
    }

    private static class ScoredProduct {
        final String source;
        final double similitud;
        final String jsonEvent;
        final Map<String, List<String>> targetMap;

        ScoredProduct(String source, double similitud, String jsonEvent, Map<String, List<String>> targetMap) {
            this.source = source;
            this.similitud = similitud;
            this.jsonEvent = jsonEvent;
            this.targetMap = targetMap;
        }

        void addToMap() {
            targetMap.computeIfAbsent(source, key -> new ArrayList<>()).add(jsonEvent);
        }
    }
}