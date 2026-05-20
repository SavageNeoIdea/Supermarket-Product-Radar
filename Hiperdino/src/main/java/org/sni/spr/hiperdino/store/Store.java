package org.sni.spr.hiperdino.store;

import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;

public interface Store {
    void storeSingleData(HiperdinoProduct product);

    void storeAllData(List<HiperdinoProduct> productList);

    void close();
}