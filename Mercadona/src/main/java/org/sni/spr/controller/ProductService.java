package org.sni.spr.controller;

import org.sni.spr.model.Product;
import java.util.List;
import java.util.function.Consumer;

public interface ProductService {
    void getProducts(List<String> ids, Consumer<Product> consumer);
}
