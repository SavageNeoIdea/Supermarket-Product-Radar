package org.sni.spr.controller;

import com.google.gson.Gson;
import org.sni.spr.model.Product;

import java.util.List;

public class MercadonaProductService implements ProductService {
    private final HttpClientManager httpClient;
    private final Gson gson = new Gson();

    public MercadonaProductService(HttpClientManager httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public List<Product> getProducts(List<String> ids) {
        return ids.stream()
                .map(this::fetchProduct)
                .toList();
    }

    private Product fetchProduct(String id) {
        String url = "https://tienda.mercadona.es/api/products/" + id;
        String json = httpClient.makeRequest(url);
        Product product = gson.fromJson(json, Product.class);
        product.buildCategories();
        return product;
    }
}