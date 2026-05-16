package controller.shoppingListApp;
import model.Product;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShoppingListBuilder {
    public Map<String, Map<String, List<Product>>> productInputSourceMap;
    public Map<String, List<Product>> productInputMap;

    public ShoppingListBuilder(){
        this.productInputMap = new LinkedHashMap<>();
    }

    public Map<String, Map<String, Product>> saveMoneyShopList() {
        List<Map<String, Product>> inputProductMaps = ProductUtils.BestProductsForAllSources(productInputMap);
        System.out.println(inputProductMaps);
        return inputProductMaps.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getSource(),
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                ));
    }

    public Map<String, Product> MercadonaShopList(){
        return Map.of();
    }

    public Map<String, Product> HiperdinoShopList(){

        return Map.of();
    }

    public void processProducts(String input, Product product) {
        productInputMap.computeIfAbsent(input, k -> new ArrayList<>())
                .add(product);
    }
}
