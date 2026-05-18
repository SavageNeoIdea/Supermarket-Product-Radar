package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.controller.feeder.parser.ProductJsonParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoFeeder implements ProductFeeder {

    private final ProductJsonParser productJsonParser;
    private final WebScraper webScraper;

    public HiperdinoFeeder(ProductJsonParser productJsonParser, WebScraper webScraper) {
        this.productJsonParser = productJsonParser;
        this.webScraper = webScraper;
    }

    @Override
    public void extractRawData(Consumer<HiperdinoProduct> productConsumer) {
        webScraper.startScraping(rawJsonList -> {
            processJsonsAndSend(rawJsonList, productConsumer);
        });
    }

    @Override
    public void processJsonsAndSend(List<String> rawJson, Consumer<HiperdinoProduct> productConsumer) {
        List<HiperdinoProduct> allProductsInBatch = productJsonParser.parse(rawJson);
        sendProducts(productConsumer, allProductsInBatch);
    }

    private void sendProducts(Consumer<HiperdinoProduct> productConsumer, List<HiperdinoProduct> allProductsInBatch) {
        Set<String> seenKeys = new HashSet<>();
        allProductsInBatch.stream()
                .filter(p -> seenKeys.add(p.getHiperdinoName() + "_" + p.getHiperdinoEan()))
                .forEach(productConsumer);
    }
}