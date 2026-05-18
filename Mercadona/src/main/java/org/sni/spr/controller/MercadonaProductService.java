package org.sni.spr.controller;

import com.google.gson.Gson;
import org.sni.spr.model.*;

import java.util.List;
import java.util.Objects;

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
                .filter(Objects::nonNull)
                .toList();
    }

    private Product fetchProduct(String id) {
        String url = "https://tienda.mercadona.es/api/products/" + id;
        try {
            String json = httpClient.makeRequest(url);
            Product product = gson.fromJson(json, Product.class);
            buildCategories(product);
            addGluten(product);
            return product;
        } catch (Exception e) {
            System.err.println("Producto ignorado (id=" + id + ") - " + e.getMessage());
            return null;
        }
    }

    private void buildCategories(Product product) {
        List<Category> categories = product.getCategories();
        if (categories == null || categories.isEmpty()) return;
        List<String> path = categories.getFirst().extractPath();
        product.setCategory(!path.isEmpty() ? path.get(0) : null);
        product.setSubcategory(path.size() > 1 ? path.get(1) : null);
        product.setSubsubcategory(path.size() > 2 ? path.get(2) : null);
    }

    private void addGluten(Product product) {
        String text;
        text = product.getMandatoryMentions() != null ? product.getMandatoryMentions() : product.getDisplayName();
        text = text.toLowerCase();
        product.setGluten(text.contains("sin gluten") ? Boolean.TRUE : Boolean.FALSE);
    }
}