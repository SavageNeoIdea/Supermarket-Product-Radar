package org.sni.businessunit.controller.feeder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.sni.businessunit.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductFeeder implements Feeder {

    private final SemanticEngine semanticEngine;
    private final Gson gson;

    public ProductFeeder(SemanticEngine semanticEngine) {
        this.semanticEngine = semanticEngine;
        this.gson = new Gson();
    }

    @Override
    public List<Product> processData(Map<String, List<String>> rawJson) {
        if (jsonIsEmpty(rawJson)) return Collections.emptyList();
        List<Product> rawProducts = extractAllProducts(rawJson);
        enrichWithEmbeddingsConcurrently(rawProducts);
        return rawProducts;
    }

    private List<Product> extractAllProducts(Map<String, List<String>> rawJson) {
        List<Product> productList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : rawJson.entrySet()) {
            String source = entry.getKey();
            List<String> events = entry.getValue();
            if (events == null) continue;
            for (String event : events) {
                Product product = parseSingleEvent(source, event);
                if (product != null) {
                    productList.add(product);
                }
            }
        }
        return productList;
    }

    @Override
    public Product processData(String source, String event) {
        return parseSingleEvent(source, event);
    }

    private Product parseSingleEvent(String source, String event) {
        if (event == null || event.isBlank()) return null;
        try {
            JsonObject root = JsonParser.parseString(event).getAsJsonObject();
            if (!root.has("payload")) return null;
            String ts = extractField(root, "ts");
            JsonObject payload = root.getAsJsonObject("payload");
            return mapPayloadToProduct(payload, source, ts);
        } catch (Exception e) {
            System.err.printf("Error procesando evento de [%s]: %s%n", source, e.getMessage());
            return null;
        }
    }

    private Product mapPayloadToProduct(JsonObject payload, String source, String ts) {
        String name = payload.get(source + "Name").getAsString();
        double price = payload.get(source + "Price").getAsDouble();
        String measure = payload.get(source + "Measure").getAsString();
        int qty = payload.get(source + "Qty").getAsInt();
        int pQty = payload.get(source + "PackageQty").getAsInt();
        String brand = payload.get(source + "Brand").getAsString();
        String ean = extractField(payload, source + "Ean");

        return new Product(name, price, measure, qty, pQty, ean, brand, source, ts);
    }

    private String extractField(JsonObject json, String memberName) {
        if (json == null || !json.has(memberName)) return "";
        JsonElement element = json.get(memberName);
        return element.isJsonNull() ? "" : element.getAsString();
    }

    private void enrichWithEmbeddingsConcurrently(List<Product> products) {
        products.parallelStream().forEach(product -> {
            try {
                product.generateEmbedding(semanticEngine, gson);
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

    private boolean jsonIsEmpty(Map<String, List<String>> rawJson) {
        return rawJson == null || rawJson.isEmpty();
    }
}