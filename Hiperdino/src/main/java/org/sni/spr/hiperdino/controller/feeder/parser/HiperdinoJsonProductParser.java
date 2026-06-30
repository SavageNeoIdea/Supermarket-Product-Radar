package org.sni.spr.hiperdino.controller.feeder.parser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.RawCategoryProductBatch;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class HiperdinoJsonProductParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HiperdinoJsonProductParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<HiperdinoProduct> parse(RawCategoryProductBatch payload) {
        if (!jsonHasData(payload))
            return List.of();
        try {
            JsonNode json = readJson(payload);
            JsonNode productsNode = extractProductsNode(json);
            return extractAllProducts(productsNode, payload);

        } catch (Exception e) {
            System.err.println("Error processing Hiperdino JSON: " + e.getMessage());
            return List.of();
        }
    }


    private static boolean jsonHasData(RawCategoryProductBatch payload) {
        return payload != null &&
                payload.jsonBody() != null &&
                !payload.jsonBody().isBlank();
    }

    private static JsonNode readJson(RawCategoryProductBatch payload) throws Exception {
        return MAPPER.readTree(payload.jsonBody());
    }

    private static JsonNode extractProductsNode(JsonNode root) {
        return root.has("productGtmData") ? root.get("productGtmData") : root;
    }

    private static List<HiperdinoProduct> extractAllProducts(JsonNode productsNode, RawCategoryProductBatch payload) {
        List<HiperdinoProduct> products = new ArrayList<>();
        productsNode.fields().forEachRemaining(entry -> products.addAll(createProductsFromNode(entry.getValue(), payload)));
        return products;
    }

    private static List<HiperdinoProduct> createProductsFromNode(JsonNode node, RawCategoryProductBatch payload) {
        String eanField = node.path("ean").asText();

        if (isValidEan(eanField)) {
            ParsedProductName parsedName = HiperdinoProductNameParser.parse(node.path("name").asText());
            List<HiperdinoProduct> products = new ArrayList<>();
            String[] eans = eanField.split("\\s*,\\s*");

            String cleanName = sanitizeText(parsedName.name());
            String cleanBrand = sanitizeText(node.path("label_brand").asText());
            String cleanCategory = sanitizeText(payload.category());
            String cleanSubcategory = sanitizeText(payload.subcategory());

            for (String ean : eans) {
                products.add(new HiperdinoProduct(
                        node.path("sku").asText(),
                        ean,
                        cleanBrand,
                        cleanCategory,
                        cleanSubcategory,
                        cleanName,
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
        return List.of();
    }

    private static boolean isValidEan(String eanField){
        return !(eanField == null || eanField.isBlank());
    }

    private static String sanitizeText(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String text = input.trim().toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        text = text.replaceAll("[^\\p{L}\\p{N}\\s]", "");
        text = text.replaceAll("\\s{2,}", " ").trim();
        return text;
    }
    private static double getRawPriceAsDouble(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return 0.0;
        try {
            return Double.parseDouble(priceStr.replace(",", "."));
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir precio: " + priceStr);
            return 0.0;
        }
    }
}