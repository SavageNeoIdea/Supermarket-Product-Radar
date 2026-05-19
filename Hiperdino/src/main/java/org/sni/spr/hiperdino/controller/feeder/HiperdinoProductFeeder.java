package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.controller.feeder.parser.ProductJsonParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoProductFeeder implements ProductFeeder {

    private final ProductJsonParser productJsonParser;
    private final WebScraper webScraper;

    public HiperdinoProductFeeder(ProductJsonParser productJsonParser, WebScraper webScraper) {
        this.productJsonParser = productJsonParser;
        this.webScraper = webScraper;
    }

    @Override
    public void extractNewProducts(Consumer<HiperdinoProduct> productConsumer) {
        webScraper.startScraping(getJsonConsumer(productConsumer));
    }

    private Consumer<List<String>> getJsonConsumer(Consumer<HiperdinoProduct> productConsumer){
        return obtainedRawJsonList -> {
            consumeProducts(productConsumer, transformToProduct(obtainedRawJsonList));
        };
    }

    private void consumeProducts(Consumer<HiperdinoProduct> productConsumer, List<HiperdinoProduct> allProductsInBatch) {
        Set<String> seenKeys = new HashSet<>();
        allProductsInBatch.stream()
                .filter(p -> seenKeys.add(p.getHiperdinoName() + "_" + p.getHiperdinoEan()))
                .forEach(productConsumer);
    }

    private List<HiperdinoProduct> transformToProduct(List<String> rawJson){
        return productJsonParser.parse(rawJson);
    }
}