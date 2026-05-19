package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.controller.feeder.parser.ProductJsonParser;
import org.sni.spr.hiperdino.controller.feeder.parser.ScraperRawPayload;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoProductFeeder implements ProductFeeder {

    private final ProductJsonParser productJsonParser;

    public HiperdinoProductFeeder(ProductJsonParser productJsonParser) {
        this.productJsonParser = productJsonParser;
    }

    @Override
    public void feed(ScraperRawPayload scraperRawPayloads, Consumer<HiperdinoProduct> productConsumer) {
        List<HiperdinoProduct> hiperdinoProductList = transformToProduct(scraperRawPayloads);
        consumeProducts(hiperdinoProductList, productConsumer);
    }

    private void consumeProducts(List<HiperdinoProduct> allProductsInBatch, Consumer<HiperdinoProduct> productConsumer) {
        Set<String> seenKeys = new HashSet<>();
        allProductsInBatch.stream()
                .filter(p -> seenKeys.add(p.getHiperdinoName() + "_" + p.getHiperdinoEan()))
                .forEach(productConsumer);
    }

    private List<HiperdinoProduct> transformToProduct(ScraperRawPayload scraperRawPayloads){
        return productJsonParser.parse(scraperRawPayloads);
    }
}