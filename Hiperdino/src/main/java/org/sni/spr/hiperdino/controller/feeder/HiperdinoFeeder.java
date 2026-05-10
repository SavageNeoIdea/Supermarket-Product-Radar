package org.sni.spr.hiperdino.controller.feeder;

import org.sni.spr.hiperdino.controller.feeder.parser.ProductParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiperdinoFeeder implements ProductFeeder {

    private final ProductParser productParser;
    private final WebScraper webScraper;
    private final List<HiperdinoProduct> productList = new ArrayList<>();

    public HiperdinoFeeder(ProductParser productParser, WebScraper webScraper) {
        this.productParser = productParser;
        this.webScraper = webScraper;
    }

    @Override
    public List<HiperdinoProduct> extractAndTransformProducts() {
        List<Map<String, String>> rawProductsList = webScraper.extractProductRawData();
        for (Map<String, String> rawProductData : rawProductsList) {
            productList.addAll(formatProduct(rawProductData));
        }

        return productList;
    }

    private List<HiperdinoProduct> formatProduct(Map<String, String> rawProduct) {
        productParser.identify(rawProduct.get("name"));
        List<HiperdinoProduct> hiperdinoProductList = new ArrayList<>();
        if (rawProduct.get("ean") == null || rawProduct.get("ean").isBlank())
            return List.of();
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