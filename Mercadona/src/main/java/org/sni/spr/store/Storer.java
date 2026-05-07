package org.sni.spr.store;

import org.sni.spr.model.Product;
import java.util.List;

public interface Storer {
    void saveAll(List<Product> products);
}
