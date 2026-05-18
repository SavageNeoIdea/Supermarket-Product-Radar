package org.sni.spr.controller;

import org.sni.spr.model.Product;
import java.util.List;

public interface ProductService {
    List<Product> getProducts(List<String> ids);
}
