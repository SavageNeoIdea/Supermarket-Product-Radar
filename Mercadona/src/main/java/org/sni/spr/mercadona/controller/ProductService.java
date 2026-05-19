package org.sni.spr.mercadona.controller;

import org.sni.spr.mercadona.model.Product;
import java.util.List;
import java.util.function.Consumer;

public interface ProductService {
    void getProducts(List<String> ids, Consumer<Product> consumer);
}
