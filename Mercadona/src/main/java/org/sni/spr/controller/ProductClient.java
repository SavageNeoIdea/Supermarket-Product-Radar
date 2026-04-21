package org.sni.spr.controller;

import com.google.gson.Gson;
import org.sni.spr.model.Product;

public class ProductClient {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ProductClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Product getProduct(String id) {
        try {
            String json = httpClient.makeRequest("https://tienda.mercadona.es/api/products/" + id);
            return gson.fromJson(json, Product.class);
        } catch (Exception e) {
            return null;
        }
    }
}