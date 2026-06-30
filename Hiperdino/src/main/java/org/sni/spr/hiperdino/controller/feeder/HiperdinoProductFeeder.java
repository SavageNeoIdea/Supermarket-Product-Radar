package org.sni.spr.hiperdino.controller.feeder;

import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoJsonProductParser;
import org.sni.spr.hiperdino.model.RawCategoryProductBatch;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class HiperdinoProductFeeder implements ProductFeeder {

    public HiperdinoProductFeeder() {
    }

    @Override
    public void feed(RawCategoryProductBatch productJsonBatch, Consumer<HiperdinoProduct> productConsumer) {
        List<HiperdinoProduct> hiperdinoProductList = transformToProduct(productJsonBatch);
        consumeProducts(hiperdinoProductList, productConsumer);
    }

    private void consumeProducts(List<HiperdinoProduct> allProductsInBatch, Consumer<HiperdinoProduct> productConsumer) {
        Set<String> seenKeys = new HashSet<>();
        allProductsInBatch.stream()
                .filter(p -> seenKeys.add(p.getHiperdinoName() + "_" + p.getHiperdinoEan()))
                .forEach(productConsumer);
    }

    private List<HiperdinoProduct> transformToProduct(RawCategoryProductBatch ProductBatch) {
        return HiperdinoJsonProductParser.parse(ProductBatch);
    }
}