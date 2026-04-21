package org.sni.spr.hiperdino.controller.feeder;

import org.sni.spr.hiperdino.controller.feeder.parser.ProductParser;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiperdinoFeeder implements ProductFeeder {

    private final ProductParser productParser;
    private final WebScraper webScraper;
    public HiperdinoFeeder(ProductParser productParser, WebScraper webScraper) {
        this.productParser = productParser;
        this.webScraper = webScraper;
    }

    @Override
    public List<HiperdinoProduct> getProducts() {
        List<Map<String, String>> rawProductsList = webScraper.extractProductRawData();
        List<HiperdinoProduct> productList = new ArrayList<>();
        for (Map<String, String> rawProductData : rawProductsList) {
            productList.add(formatProduct(rawProductData));
        }
        return productList;
    }

    private HiperdinoProduct formatProduct(Map<String, String> rawProduct) {
        productParser.identify(rawProduct.get("name"));
        String name = productParser.getName();
        String priceStr = rawProduct.get("price");
        double price = 0.0;
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr.replace(",", "."));
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir precio: " + priceStr);
            }
        }
        int packageQty = productParser.getPackageQty();
        int qty = productParser.getQty();
        UnitsOfMeasurement measure = productParser.getMeasure();
        String sku = rawProduct.get("sku");
        String ean = rawProduct.get("ean");
        String brand = rawProduct.get("brand");
        String sapId = rawProduct.get("sap_id");
        String category = rawProduct.get("category");
        String subcategory = rawProduct.get("subcategory");
        boolean gluten = Boolean.parseBoolean(rawProduct.get("gluten"));

        String urlImage = rawProduct.get("image_url");

        return new HiperdinoProduct(
                sku,
                ean,
                sapId,
                brand,
                category,
                subcategory,
                name,
                qty,
                packageQty,
                measure,
                price,
                gluten,
                urlImage
        );
    }
}