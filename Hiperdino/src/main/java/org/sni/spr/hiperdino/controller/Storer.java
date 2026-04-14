package org.sni.spr.hiperdino.controller;

import org.sni.spr.hiperdino.model.Product;

import java.util.List;
import java.util.Map;

public interface Storer {
    public void storeAllData(Map<String, List<Product>> productsMap);
}
