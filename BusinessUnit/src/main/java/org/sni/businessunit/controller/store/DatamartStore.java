package org.sni.businessunit.controller.store;

import org.sni.businessunit.model.Product;

import java.util.List;

public interface DatamartStore {
    void storeAllData(List<Product> products);
}
