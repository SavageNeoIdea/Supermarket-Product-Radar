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
        Set<String> sources = rawJson.keySet();

        for (String source : sources) {
            for (String event : rawJson.get(source)) {
                JsonObject json = JsonParser.parseString(event).getAsJsonObject();
                String productName = json.get(source + "Name").getAsString();
                double productPrice = json.get(source + "Price").getAsDouble();
                String productMeasure = json.get(source + "Measure").getAsString();
                int productQty = json.get(source + "Quantity").getAsInt();
                int productPackageQty = json.get(source + "PackageQuantity").getAsInt();
                String productEan = json.get(source + "Ean").getAsString();
                String productBrand = json.get(source + "Brand").getAsString();
                String productSource = source;

                productList.add(new Product(
                        productName,
                        productPrice,
                        productMeasure,
                        productQty,
                        productPackageQty,
                        productEan,
                        productBrand,
                        productSource
                        )
                );
            }

        }
        return productList;
    }

    @Override
    public Product processData(String source, String event) {
        JsonObject json = JsonParser.parseString(event).getAsJsonObject();
        String productName = json.get(source + "Name").getAsString();
        double productPrice = json.get(source + "Price").getAsDouble();
        String productMeasure = json.get(source + "Measure").getAsString();
        int productQty = json.get(source + "Quantity").getAsInt();
        int productPackageQty = json.get(source + "PackageQuantity").getAsInt();
        String productEan = json.get(source + "Ean").getAsString();
        String productBrand = json.get(source + "Brand").getAsString();
        String productSource = source;

        return new Product(
                        productName,
                        productPrice,
                        productMeasure,
                        productQty,
                        productPackageQty,
                        productEan,
                        productBrand,
                        productSource
        );
    }
}
