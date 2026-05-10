package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;
import java.util.function.Consumer;

public interface ProductFeeder {
    void extractAndStream(Consumer<HiperdinoProduct> productConsumer);
    void processRawAndEmit(List<String> rawJson, Consumer<HiperdinoProduct> productConsumer);
}
