package org.sni.spr.hiperdino.view;

import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.Product;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HiperdinoFeeder implements ProductFeeder{

    private static final int LOWER_LIMIT = 12;
    private static final int DUPLICATED_MENU_OFFSET = 139;
    private final ProductParser productParser;
    private Map<Integer, List<String>> productRawData;

    public HiperdinoFeeder(ProductParser productParser){
        this.productParser = productParser;
    }

    @Override
    public Map<String, List<Product>> getProducts(Map<String, List<Map<String, String>>> productsRawDataMap){
        Map<String, List<Product>> productsMap = new LinkedHashMap<>();

        productsRawDataMap.forEach((key, value) -> {
            productsMap.put(key, getCategoryProducts(value));
        });

        return productsMap;
    }

    private List<Product> getCategoryProducts(List<Map<String, String>> productRawData) {
        if (productRawData.isEmpty()) return List.of();

        String category = productRawData.getFirst().get("category");
        String subcategory = productRawData.getFirst().get("subcategory");

        List<Product> productList = new ArrayList<>();

        for (Map<String, String> productMetadata : productRawData) {
            String rawName = productMetadata.get("name");
            System.out.println(rawName);
            String rawPrice = productMetadata.get("price");
            productParser.identify(rawName, rawPrice);

            String name = productParser.getName();
            System.out.println(name);
            int qty = productParser.getQty();
            int packageQty = productParser.getPackageQty();
            UnitsOfMeasurement measure = productParser.getMeasure();
            double price = productParser.getPrice();
            System.out.println(price);

            boolean gluten = Boolean.parseBoolean(productMetadata.get("gluten"));
            String urlImage = productMetadata.get("urlImage");

            productList.add(new HiperdinoProduct(
                    category,
                    subcategory,
                    name,
                    qty,
                    packageQty,
                    measure,
                    price,
                    gluten,
                    urlImage
            ));
            System.out.println(productList.getLast());
        }
        return productList;
    }
}

