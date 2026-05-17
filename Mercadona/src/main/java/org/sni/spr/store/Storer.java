package org.sni.spr.store;

import org.sni.spr.model.Product;
import java.util.List;

public interface Storer extends AutoCloseable{
    void saveAll(List<Product> products);

    @Override
    void close();
}
