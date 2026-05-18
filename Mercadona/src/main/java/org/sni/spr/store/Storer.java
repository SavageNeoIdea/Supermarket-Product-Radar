package org.sni.spr.store;

import org.sni.spr.model.Product;

public interface Storer extends AutoCloseable{
    void save(Product product);

    @Override
    void close();
}
