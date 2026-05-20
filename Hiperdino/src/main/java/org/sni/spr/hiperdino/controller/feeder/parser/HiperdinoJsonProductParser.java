package org.sni.spr.hiperdino.controller.feeder.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.ArrayList;
import java.util.List;

public class HiperdinoJsonProductParser implements ProductJsonParser {

    private final ObjectMapper mapper;
    private final ProductNameParser nameParser;

    public HiperdinoJsonProductParser(ObjectMapper mapper, ProductNameParser nameParser) {
        this.mapper = mapper;
        this.nameParser = nameParser;
    }

    @Override
    public List<HiperdinoProduct> parse(ScraperRawPayload payload) {
        List<HiperdinoProduct> allProductsInBatch = new ArrayList<>();
        if (payload == null || payload.jsonBody() == null || payload.jsonBody().isBlank()) {
            return allProductsInBatch;
        }

        try {
            JsonNode root = mapper.readTree(payload.jsonBody());
            JsonNode productsNode = root.has("productGtmData") ? root.get("productGtmData") : root;

            productsNode.fields().forEachRemaining(entry -> {
                List<HiperdinoProduct> products = createProductsFromNode(entry.getValue(), payload);
                allProductsInBatch.addAll(products);
            });
        } catch (Exception e) {
            System.err.println("Error procesando JSON de Hiperdino: " + e.getMessage());
        }
        return allProductsInBatch;
    }

    private List<HiperdinoProduct> createProductsFromNode(JsonNode node, ScraperRawPayload payload) {
        String eanField = node.path("ean").asText();
        if (eanField == null || eanField.isBlank()) {
            return List.of();
        }

        ParsedName parsedName = nameParser.parse(node.path("name").asText());

        List<HiperdinoProduct> products = new ArrayList<>();
        String[] eans = eanField.split("\\s*,\\s*");

        for (String ean : eans) {
            products.add(new HiperdinoProduct(
                    node.path("sku").asText(),
                    ean,
                    node.path("label_brand").asText(),
                    payload.category(),
                    payload.subcategory(),
                    parsedName.name(),
                    parsedName.qty(),
                    parsedName.packageQty(),
                    parsedName.measure(),
                    getRawPriceAsDouble(node.path("final_price").asText()),
                    node.path("sin_gluten").asBoolean(),
                    node.path("image").asText()
            ));
        }
        return products;
    }

    private double getRawPriceAsDouble(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return 0.0;
        try {
            return Double.parseDouble(priceStr.replace(",", "."));
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir precio: " + priceStr);
            return 0.0;
        }
    }
}