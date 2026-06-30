package org.sni.businessunit.controller.shoppinglist;

import com.google.gson.Gson;
import org.sni.businessunit.controller.embedding.SemanticEngine;
import org.sni.businessunit.model.OptimizedItem;
import org.sni.businessunit.model.Product;
import org.sni.businessunit.store.sqlite.SQLiteConnection;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLiteQuery implements SearchQuery {

    private static final int MAX_RESULTS_PER_MARKET = 50;
    private static final int BEST_PRODUCTS_LIMIT = 7;
    private static final int CACHE_CHECK_INTERVAL_MINUTES = 5;
    private static final Comparator<Product> BEST_FIRST_COMPARATOR =
            Comparator.comparingDouble(Product::getSimilarityScore).reversed();

    private final ProductCharger productCharger;
    private final SemanticEngine semanticEngine;
    private final Gson gson = new Gson();

    private List<Product> catalogCache;
    private LocalDateTime lastCheckTime;
    private Instant cacheMaxTs;

    public SQLiteQuery(SQLiteConnection sqliteConn, SemanticEngine semanticEngine) {
        this.productCharger = new ProductCharger(sqliteConn);
        this.semanticEngine = semanticEngine;
    }

    @Override
    public OptimizedItem searchQuery(String userInput) {
        if (isInvalidInput(userInput)) return null;

        refreshCacheIfNeeded();

        List<Product> mercadonaCandidates = findTopMatchingProductsBySource(userInput, "mercadona");
        List<Product> hiperdinoCandidates = findTopMatchingProductsBySource(userInput, "hiperdino");

        List<Product> mercadonaBests = getBestBySource(mercadonaCandidates, "mercadona");
        List<Product> hiperdinoBests = getBestBySource(hiperdinoCandidates, "hiperdino");
        List<Product> jointBest = selectGlobalBest(mercadonaBests, hiperdinoBests);

        return new OptimizedItem(userInput, jointBest, mercadonaBests, hiperdinoBests);
    }

    private boolean isInvalidInput(String userInput) {
        return userInput == null || userInput.isBlank();
    }

    private synchronized void refreshCacheIfNeeded() {
        if (catalogCache == null) {
            reloadCache();
            return;
        }

        if (Duration.between(lastCheckTime, LocalDateTime.now()).toMinutes() >= CACHE_CHECK_INTERVAL_MINUTES) {
            String latestDbTs = productCharger.getLatestTimestamp();
            if (latestDbTs != null && !latestDbTs.isBlank()) {
                Instant dbMaxTs = Instant.parse(latestDbTs);
                if (dbMaxTs.isAfter(cacheMaxTs)) {
                    System.out.println("INFO: Nuevos productos detectados (TS: " + latestDbTs + "). Actualizando caché...");
                    reloadCache();
                    return;
                }
            }
            lastCheckTime = LocalDateTime.now();
        }
    }

    private void reloadCache() {
        System.out.println("INFO: Cargando catálogo de productos en memoria...");
        catalogCache = productCharger.fetchAllProductsFromDatabase();
        cacheMaxTs = catalogCache.stream()
                .map(p -> Instant.parse(p.getTs()))
                .max(Instant::compareTo)
                .orElse(Instant.MIN);
        lastCheckTime = LocalDateTime.now();
    }

    private List<Product> findTopMatchingProductsBySource(String input, String source) {
        float[] queryVector = semanticEngine.embedInput(input);

        return catalogCache.parallelStream()
                .filter(product -> source.equalsIgnoreCase(product.getSource()))
                .map(product -> {
                    try {
                        float[] productVector = gson.fromJson(product.getEmbeddingVector(), float[].class);
                        Double score = semanticEngine.calculateHybridScore(productVector, product.getName(), queryVector, input);
                        if (score != null) {
                            product.setSimilarityScore(score);
                            return product;
                        }
                    } catch (Exception e) {
                        System.err.printf("Error al procesar producto [%s]: %s%n", product.getName(), e.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(Product::getSimilarityScore).reversed())
                .limit(MAX_RESULTS_PER_MARKET)
                .collect(Collectors.toList());
    }

    private List<Product> getBestBySource(List<Product> candidates, String source) {
        return candidates.stream()
                .filter(p -> source.equalsIgnoreCase(p.getSource()))
                .sorted(BEST_FIRST_COMPARATOR)
                .limit(BEST_PRODUCTS_LIMIT)
                .collect(Collectors.toList());
    }

    private List<Product> selectGlobalBest(List<Product> p1, List<Product> p2) {
        return Stream.concat(p1.stream(), p2.stream())
                .sorted(BEST_FIRST_COMPARATOR)
                .limit(BEST_PRODUCTS_LIMIT)
                .collect(Collectors.toList());
    }
}