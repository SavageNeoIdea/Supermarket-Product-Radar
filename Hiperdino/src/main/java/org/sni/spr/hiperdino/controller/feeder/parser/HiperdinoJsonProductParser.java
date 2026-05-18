package org.sni.spr.hiperdino.controller.feeder.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.*;

public class HiperdinoJsonProductParser implements ProductJsonParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProductNameParser nameParser;

    public HiperdinoJsonProductParser(ProductNameParser nameParser) {
        this.nameParser = nameParser;
    }

    @Override
    public List<HiperdinoProduct> parse(List<String> rawJson) {
        List<HiperdinoProduct> allProductsInBatch = new ArrayList<>();
        if (rawJson == null || rawJson.isEmpty()) {
            return allProductsInBatch;
        }

        try {
            JsonNode root = mapper.readTree(rawJson.getFirst());
            JsonNode productsNode = root.has("productGtmData") ? root.get("productGtmData") : root;
            productsNode.fields().forEachRemaining(entry -> {
                JsonNode p = entry.getValue();
                List<HiperdinoProduct> products = createProductsFromNode(p, rawJson);
                allProductsInBatch.addAll(products);
            });
        } catch (Exception e) {
            System.err.println("Error procesando JSON de Hiperdino: " + e.getMessage());
        }
        return allProductsInBatch;
    }

    private List<HiperdinoProduct> createProductsFromNode(JsonNode node, List<String> rawJson) {
        String eanField = node.path("ean").asText();
        if (eanField == null || eanField.isBlank()) {
            return List.of();
        }
        nameParser.identify(node.path("name").asText());
        List<HiperdinoProduct> products = new ArrayList<>();
        String[] eans = eanField.split("\\s*,\\s*");

        for (String ean : eans) {
            products.add(new HiperdinoProduct(
                    node.path("sku").asText(),
                    ean,
                    node.path("label_brand").asText(),
                    rawJson.get(1),
                    rawJson.getLast(),
                    nameParser.getName(),
                    nameParser.getQty(),
                    nameParser.getPackageQty(),
                    nameParser.getMeasure(),
                    getRawPriceAsDouble(node.path("final_price").asText()),
                    node.path("sin_gluten").asBoolean(),
                    node.path("image").asText()
            ));
        }
        return products;
    }

    public double getRawPriceAsDouble(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(priceStr.replace(",", "."));
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir precio: " + priceStr);
            return 0.0;
        }
    }
}