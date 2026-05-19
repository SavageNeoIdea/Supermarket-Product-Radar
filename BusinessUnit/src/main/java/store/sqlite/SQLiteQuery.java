package store.sqlite;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import store.EmbeddingService;
import store.SearchQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SQLiteQuery implements SearchQuery {

    private final SQLiteConnection sqLiteConnection;
    private final EmbeddingService iaService;
    private final Gson gson;
    private final ExecutorService executorService;
    private static final double SIMILARITY_THRESHOLD = 0.45;
    private static final int MAX_RESULTS = 15;
    private static final long QUERY_TIMEOUT_SECONDS = 10;

    public SQLiteQuery(SQLiteConnection sqLiteConnection, EmbeddingService iaService) {
        this.sqLiteConnection = sqLiteConnection;
        this.iaService = iaService;
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Map<String, Map<String, List<String>>> searchQuery(String input) {
        Map<String, Map<String, List<String>>> data = new LinkedHashMap<>();
        if (isInputEmpty(input)) {
            data.put("", new HashMap<>());
            return data;
        }

        float[] queryVector = iaService.getEmbeddingVector(input);
        Map<String, List<String>> sourceEventMap = new HashMap<>();
        String sql = "SELECT source, name, price, measure, quantity, packageQuantity, brand, embedding_vector FROM product";

        List<RowData> rows = readAllRows(sql);
        if (rows.isEmpty()) {
            data.put(input, sourceEventMap);
            return data;
        }

        List<Callable<ScoredProduct>> tasks = buildSimilarityTasks(rows, queryVector, sourceEventMap);
        try {
            List<Future<ScoredProduct>> futures = executorService.invokeAll(tasks, QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            List<ScoredProduct> candidatos = new ArrayList<>();
            for (Future<ScoredProduct> f : futures) {
                if (f.isCancelled()) continue;
                try {
                    ScoredProduct sp = f.get();
                    if (sp != null) candidatos.add(sp);
                } catch (ExecutionException e) {
                    logError("Error en tarea de similitud: " + e.getCause().getMessage());
                }
            }
            collectTopResults(candidatos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("El proceso de búsqueda multihilo fue interrumpido", e);
        }

        data.put(input, sourceEventMap);
        return data;
    }

    private boolean isInputEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    private List<RowData> readAllRows(String sqlQuery) {
        List<RowData> rows = new ArrayList<>();
        try (Connection conn = sqLiteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String embeddingStr = rs.getString("embedding_vector");
                if (embeddingStr == null || embeddingStr.isBlank()) continue;
                rows.add(new RowData(
                        rs.getString("source"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("measure"),
                        rs.getInt("quantity"),
                        rs.getInt("packageQuantity"),
                        rs.getString("brand"),
                        embeddingStr
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la lectura en la base de datos", e);
        }
        return rows;
    }

    private List<Callable<ScoredProduct>> buildSimilarityTasks(List<RowData> rows, float[] queryVector, Map<String, List<String>> targetMap) {
        List<Callable<ScoredProduct>> tasks = new ArrayList<>(rows.size());
        for (RowData row : rows) {
            tasks.add(() -> {
                try {
                    float[] productVector = gson.fromJson(row.embeddingJson, float[].class);
                    double similitud = iaService.computeRelevanceScore(queryVector, productVector);
                    if (similitud > SIMILARITY_THRESHOLD) {
                        String jsonEvent = buildJsonEvent(row.source, row.name, row.price, row.measure, row.quantity, row.packageQuantity, row.brand, similitud);
                        return new ScoredProduct(row.source, similitud, jsonEvent, targetMap);
                    }
                } catch (Exception e) {
                    logError("Error calculando similitud para producto " + row.name + " - " + e.getMessage());
                }
                return null;
            });
        }
        return tasks;
    }

    private void collectTopResults(List<ScoredProduct> candidatos) {
        candidatos.sort((a, b) -> Double.compare(b.similitud, a.similitud));
        int limit = Math.min(MAX_RESULTS, candidatos.size());
        for (int i = 0; i < limit; i++) {
            candidatos.get(i).addToMap();
        }
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

    private void logInfo(String msg) {
        System.out.println(msg);
    }

    private void logError(String msg) {
        System.err.println(msg);
    }

    private static class RowData {
        final String source;
        final String name;
        final double price;
        final String measure;
        final int quantity;
        final int packageQuantity;
        final String brand;
        final String embeddingJson;

        RowData(String source, String name, double price, String measure, int quantity, int packageQuantity, String brand, String embeddingJson) {
            this.source = source;
            this.name = name;
            this.price = price;
            this.measure = measure;
            this.quantity = quantity;
            this.packageQuantity = packageQuantity;
            this.brand = brand;
            this.embeddingJson = embeddingJson;
        }
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
            targetMap.computeIfAbsent(source, k -> new ArrayList<>()).add(jsonEvent);
        }
    }
}
