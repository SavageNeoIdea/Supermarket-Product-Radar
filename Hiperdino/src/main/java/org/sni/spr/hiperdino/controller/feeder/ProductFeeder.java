package org.sni.spr.hiperdino.controller.feeder;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;

public interface ProductFeeder {
    public List<HiperdinoProduct> extractTransformProduct();
}
