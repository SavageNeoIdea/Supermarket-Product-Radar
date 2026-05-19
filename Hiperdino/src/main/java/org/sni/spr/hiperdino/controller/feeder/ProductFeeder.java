package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;
import java.util.function.Consumer;

public interface ProductFeeder {
    void extractNewProducts(Consumer<HiperdinoProduct> productConsumer);
}
