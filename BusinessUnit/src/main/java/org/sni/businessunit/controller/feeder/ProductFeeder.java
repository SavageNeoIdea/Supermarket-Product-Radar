package org.sni.businessunit.controller.feeder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.sni.businessunit.controller.embedding.SemanticEngine;
import org.sni.businessunit.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductFeeder implements Feeder {

    private final SemanticEngine semanticEngine;

    public ProductFeeder(SemanticEngine semanticEngine) {
        this.semanticEngine = semanticEngine;
    }

    @Override
    public List<Product> processData(Map<String, List<String>> rawEventsPerSource) {
        if (rawEventsPerSourceIsEmpty(rawEventsPerSource)) return Collections.emptyList();
        List<Product> products = extractAllProducts(rawEventsPerSource);
        enrichWithEmbeddingsConcurrently(products);
        return products;
    }

    private boolean rawEventsPerSourceIsEmpty(Map<String, List<String>> rawEventsPerSource) {
        return rawEventsPerSource == null || rawEventsPerSource.isEmpty();
    }

    private List<Product> extractAllProducts(Map<String, List<String>> rawEventsPerSource) {
        List<Product> productList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : rawEventsPerSource.entrySet()) {
            String source = entry.getKey();
            List<String> events = entry.getValue();
            if (events == null) continue;
            for (String event : events) {
                Product product = createProductFromEvent(source, event);
                if (product != null) {
                    productList.add(product);
                }
            }
        }
        return productList;
    }

    @Override
    public Product processData(String source, String rawEvent) {
        return createProductFromEvent(source, rawEvent);
    }

    private Product createProductFromEvent(String source, String rawEvent) {
        if (eventIsNotValid(rawEvent)) return null;
        try {
            JsonObject root = JsonParser.parseString(rawEvent).getAsJsonObject();
            if (!root.has("payload")) return null;
            String ts = extractField(root, "ts");
            JsonObject payload = root.getAsJsonObject("payload");
            return mapPayloadToProduct(payload, source, ts);
        } catch (Exception e) {
            System.err.printf("Error procesando evento de [%s]: %s%n", source, e.getMessage());
            return null;
        }
    }

    private static boolean eventIsNotValid(String rawEvent) {
        return rawEvent == null || rawEvent.isBlank();
    }

    private Product mapPayloadToProduct(JsonObject payload, String source, String ts) {
        String name = extractField(payload, source + "Name");
        double price = payload.has(source + "Price") ? payload.get(source + "Price").getAsDouble() : 0.0;
        String measure = extractField(payload, source + "Measure");
        double qty = payload.has(source + "Qty") ? payload.get(source + "Qty").getAsDouble() : 1;
        int pQty = payload.has(source + "PackageQty") ? payload.get(source + "PackageQty").getAsInt() : 1;
        String brand = extractField(payload, source + "Brand");
        String ean = extractField(payload, source + "Ean");
        return new Product(name, price, measure, qty, pQty, ean, brand, source, ts);
    }

    private String extractField(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        return (element != null && element.isJsonPrimitive()) ? element.getAsString() : "";
    }

    private void enrichWithEmbeddingsConcurrently(List<Product> products) {
        products.parallelStream().forEach(product -> {
            try {
                product.setEmbeddingVector(semanticEngine.embedToString(product.getEmbeddingText()));
            } catch (Exception e) {
                System.err.printf("Error calculando embedding para [%s]: %s%n", product.getName(), e.getMessage());
            }
        });
    }

    @Override
    public String extractSourceFromJson(String eventString) {
        try {
            JsonObject root = JsonParser.parseString(eventString).getAsJsonObject();
            if (root.has("ss") && !root.get("ss").isJsonNull()) {
                return root.get("ss").getAsString();
            }
        } catch (Exception e) {
            System.out.println("Error parseando el JSON para extraer 'ss': " + e.getMessage());
        }
        return null;
    }
}