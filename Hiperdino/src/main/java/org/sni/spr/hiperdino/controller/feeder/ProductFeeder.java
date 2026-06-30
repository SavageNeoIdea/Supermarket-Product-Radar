package org.sni.spr.hiperdino.controller.feeder;

import org.sni.spr.hiperdino.model.RawCategoryProductBatch;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.function.Consumer;

public interface ProductFeeder {
    void feed(RawCategoryProductBatch productBatch, Consumer<HiperdinoProduct> productConsumer);
}
