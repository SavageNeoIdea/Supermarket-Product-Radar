package org.sni.spr.controller;

import com.google.gson.Gson;
import org.sni.spr.model.Product;

public class ProductClient {
    private final Gson gson = new Gson();
    private final HttpClient httpClient = new MercadonaHttpClient();

    public Product getProduct(String id) {
        try {
            String json = httpClient.makeRequest("https://tienda.mercadona.es/api/products/" + id);
            return gson.fromJson(json, Product.class);
        } catch (Exception e) {
            return null;
        }
    }
}