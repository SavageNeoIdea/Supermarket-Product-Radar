package org.sni.businessunit.controller.shoppinglist;
import org.sni.businessunit.controller.activemq.ConfigReader;
import org.sni.businessunit.controller.embedding.SemanticEngine;
import org.sni.businessunit.model.OptimizedItem;
import org.sni.businessunit.model.Product;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SQLiteQuery implements SearchQuery {

    private static final int MAX_RESULTS = 15;
    private static final int EXTRACTION_DURATION_HOURS = 5;
    
    private final ProductCharger productCharger;
    private final SemanticEngine semanticEngine;
    private List<Product> catalogCache;
    private LocalDateTime lastCacheUpdateTime;
    private LocalTime dataReadyTime;

    public SQLiteQuery(ProductCharger productCharger, SemanticEngine semanticEngine) {
        this.productCharger = productCharger;
        this.semanticEngine = semanticEngine;
        loadScheduleConfiguration();
    }

    private void loadScheduleConfiguration() {
        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("publishers", "hiperdino");

        if (config != null && config.containsKey("ScheduleTimeHour") && config.containsKey("ScheduleTimeMinutes")) {
            int hour = Integer.parseInt(config.get("ScheduleTimeHour"));
            int minute = Integer.parseInt(config.get("ScheduleTimeMinutes"));
            this.dataReadyTime = LocalTime.of(hour, minute).plusHours(EXTRACTION_DURATION_HOURS);
        } else {
            this.dataReadyTime = LocalTime.of(16, 30);
            System.err.println("WARN: No se pudo cargar la configuración de horarios. Usando fallback: " + dataReadyTime);
        }
    }

    @Override
    public OptimizedItem searchQuery(String input) {
        if (isInvalidInput(input)) return null;
        refreshCacheIfNeeded();
        List<Product> candidates = findTopMatchingProducts(input);
        Product mercadonaBest = getBestBySource(candidates, "mercadona");
        Product hiperdinoBest = getBestBySource(candidates, "hiperdino");
        Product jointBest = selectGlobalBest(mercadonaBest, hiperdinoBest);
        return new OptimizedItem(input, jointBest, mercadonaBest, hiperdinoBest);
    }
    private boolean isInvalidInput(String input) {
        return input == null || input.isBlank();
    }
    private synchronized void refreshCacheIfNeeded() {
        if (catalogCache == null || isCacheExpired()) {
            System.out.println("INFO: Cargando catálogo de productos en memoria...");
            catalogCache = productCharger.fetchAllProductsFromDatabase();
            lastCacheUpdateTime = LocalDateTime.now();
        }
    }

    private boolean isCacheExpired() {
        if (lastCacheUpdateTime == null) return true;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayDataReady = now.toLocalDate().atTime(dataReadyTime);
        LocalDateTime lastRequiredRefresh;
        if (now.isBefore(todayDataReady)) {
            lastRequiredRefresh = todayDataReady.minusDays(1);
        } else {
            lastRequiredRefresh = todayDataReady;
        }
        return lastCacheUpdateTime.isBefore(lastRequiredRefresh);
    }

    private List<Product> findTopMatchingProducts(String input) {
        float[] queryVector = semanticEngine.embed(input);
        String normalizedQuery = input.toLowerCase().trim();
        return catalogCache.parallelStream()
                .map(product -> semanticEngine.scoreProductHybrid(product, queryVector, normalizedQuery))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(Product::getSimilarityScore).reversed())
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    private Product getBestBySource(List<Product> candidates, String source) {
        return candidates.stream()
                .filter(p -> source.equalsIgnoreCase(p.getSource()))
                .min(Comparator.comparingDouble(Product::getSimilarityScore))
                .orElse(null);
    }

    private Product selectGlobalBest(Product p1, Product p2) {
        if (p1 == null) return p2;
        if (p2 == null) return p1;
        return p1.getSimilarityScore() < p2.getSimilarityScore() ? p1 : p2;
    }
}