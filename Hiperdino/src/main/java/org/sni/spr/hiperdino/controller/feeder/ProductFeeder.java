package org.sni.spr.hiperdino.controller.feeder;

import org.sni.spr.hiperdino.controller.feeder.parser.ScraperRawPayload;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.function.Consumer;

public interface ProductFeeder {
    void feed(ScraperRawPayload obtainedRawJsonList, Consumer<HiperdinoProduct> productConsumer);
}
