package controller.feeder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductFeeder implements Feeder {

    @Override
    public List<Product> processData(Map<String, List<String>> rawJson) {
        List<Product> productList = new ArrayList<>();

        if (rawJson == null) return productList;

        Set<String> sources = rawJson.keySet();

        for (String source : sources) {
            List<String> events = rawJson.get(source);

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

            JsonObject json = root.getAsJsonObject("payload");

            String name = json.get(source + "Name").getAsString();
            double price = json.get(source + "Price").getAsDouble();
            String measure = json.get(source + "Measure").getAsString();
            int qty = json.get(source + "Qty").getAsInt();
            int pQty = json.get(source + "PackageQty").getAsInt();

            String eanKey = source + "Ean";
            String ean = json.has(eanKey) && !json.get(eanKey).isJsonNull()
                    ? json.get(eanKey).getAsString()
                    : "";
            String brand = json.get(source + "Brand").getAsString();

            return new Product(name, price, measure, qty, pQty, ean, brand, source);

        } catch (Exception e) {
            System.err.println("Error en " + source + ": " + e.getMessage());
            return null;
        }
    }
}