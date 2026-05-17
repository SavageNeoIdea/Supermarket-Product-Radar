package org.sni.spr.hiperdino.controller.feeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sni.spr.hiperdino.controller.feeder.parser.ProductParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoFeeder implements ProductFeeder {

    private final ProductParser productParser;
    private final WebScraper webScraper;
    private final List<HiperdinoProduct> productList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public HiperdinoFeeder(ProductParser productParser, WebScraper webScraper) {
        this.productParser = productParser;
        this.webScraper = webScraper;
    }

    @Override
    public void extractAndStream(Consumer<HiperdinoProduct> productConsumer) {
        webScraper.startScraping(rawJsonList -> {
            processRawAndEmit(rawJsonList, productConsumer);
        });
    }

    @Override
    public void processRawAndEmit(List<String> rawJson, Consumer<HiperdinoProduct> productConsumer) {
        try {
            JsonNode root = mapper.readTree(rawJson.getFirst());
            JsonNode productsNode = root.has("productGtmData") ? root.get("productGtmData") : root;
            List<HiperdinoProduct> allProductsInBatch = new ArrayList<>();
            productsNode.fields().forEachRemaining(entry -> {
                JsonNode p = entry.getValue();
                Map<String, String> rawData = convertNodeToMap(p, rawJson);
                List<HiperdinoProduct> products = formatProduct(rawData);
                allProductsInBatch.addAll(products);
            });
            sendProducts(productConsumer, allProductsInBatch);
        } catch (Exception e) {
            System.err.println("Error procesando JSON de JSONs: " + e.getMessage());
        }
    }

    private void sendProducts(Consumer<HiperdinoProduct> productConsumer, List<HiperdinoProduct> allProductsInBatch) {
        Set<String> seenKeys = new HashSet<>();
        List<HiperdinoProduct> uniqueProducts = allProductsInBatch.stream()
                .filter(p -> {
                    String uniqueKey = p.getHiperdinoName() + "_" + p.getHiperdinoEan();
                    return seenKeys.add(uniqueKey);
                })
                .toList();
        uniqueProducts.forEach(productConsumer);
    }

    private Map<String, String> convertNodeToMap(JsonNode node, List<String> rawJson) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("sku", node.path("sku").asText());
        map.put("ean", node.path("ean").asText());
        map.put("brand", node.path("label_brand").asText());
        map.put("category", rawJson.get(1));
        map.put("subcategory", rawJson.getLast());
        map.put("name", node.path("name").asText());
        map.put("price", node.path("final_price").asText());
        map.put("image_url", node.path("image").asText());
        map.put("gluten", node.path("sin_gluten").asText());
        return map;
    }

    private List<HiperdinoProduct> formatProduct(Map<String, String> rawProduct) {
        productParser.identify(rawProduct.get("name"));
        List<HiperdinoProduct> hiperdinoProductList = new ArrayList<>();
        if (rawProduct.get("ean") == null || rawProduct.get("ean").isBlank()) return List.of();

        String[] eans = rawProduct.get("ean").split("\\s*,\\s*");
        for (String ean : eans){
            hiperdinoProductList.add(new HiperdinoProduct(
                    rawProduct.get("sku"),
                    ean,
                    rawProduct.get("brand"),
                    rawProduct.get("category"),
                    rawProduct.get("subcategory"),
                    productParser.getName(),
                    productParser.getQty(),
                    productParser.getPackageQty(),
                    productParser.getMeasure(),
                    productParser.getRawPriceAsDouble(rawProduct.get("price")),
                    Boolean.parseBoolean(rawProduct.get("gluten")),
                    rawProduct.get("image_url")
            ));
        }
        return hiperdinoProductList;
    }
}
