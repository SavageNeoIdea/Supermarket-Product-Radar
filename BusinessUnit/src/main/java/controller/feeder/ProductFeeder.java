package controller.feeder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductFeeder implements Feeder {

    @Override
    public List<Product> processData(Map<String, List<String>> rawJson) {
        List<Product> productList = new ArrayList<>();
        if (rawJson == null) return productList;
        for (Map.Entry<String, List<String>> entry : rawJson.entrySet()) {
            String source = entry.getKey();
            List<String> events = entry.getValue();
            if (events == null) continue;
            for (String event : events) {
                Product product = processData(source, event);
                if (product != null) {
                    productList.add(product);
                }
            }
        }
        return productList;
    }

    @Override
    public Product processData(String source, String event) {
        if (event == null || event.trim().isEmpty()) return null;
        try {
            JsonObject root = JsonParser.parseString(event).getAsJsonObject();
            if (!root.has("payload")) return null;
            String ts = extractOptionalRootString(root, "ts");
            JsonObject payload = root.getAsJsonObject("payload");
            return mapPayloadToProduct(payload, source, ts);
        } catch (Exception e) {
            System.err.println("Error procesando evento de " + source + ": " + e.getMessage());
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
        String ean = extractOptionalPayloadString(payload, source + "Ean");
        return new Product(name, price, measure, qty, pQty, ean, brand, source, ts);
    }

    private String extractOptionalRootString(JsonObject root, String memberName) {
        return root.has(memberName) && !root.get(memberName).isJsonNull()
                ? root.get(memberName).getAsString()
                : "";
    }

    private String extractOptionalPayloadString(JsonObject payload, String memberName) {
        return payload.has(memberName) && !payload.get(memberName).isJsonNull()
                ? payload.get(memberName).getAsString()
                : "";
    }
}