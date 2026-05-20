package org.sni.spr.mercadona.store;

import org.sni.spr.mercadona.model.Product;

public interface Storer extends AutoCloseable {
    void save(Product product);

    @Override
    void close();
}
