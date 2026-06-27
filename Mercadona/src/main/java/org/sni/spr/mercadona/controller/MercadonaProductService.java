package org.sni.spr.mercadona.controller;

import com.google.gson.Gson;
import org.sni.spr.mercadona.model.Category;
import org.sni.spr.mercadona.model.Product;

import java.text.Normalizer;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MercadonaProductService implements ProductService {
    private final HttpClientManager httpClient;
    private final Gson gson = new Gson();

    public MercadonaProductService(HttpClientManager httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void getProducts(List<String> ids, Consumer<Product> consumer) {
        ids.forEach(id -> {
            try {
                Thread.sleep(400 + new Random().nextInt(200));
                Product product = fetchProduct(id);
                if (product != null) {
                    consumer.accept(product);
                }
            } catch (Exception e) {
                System.err.println("Error procesando id=" + id + " - " + e.getMessage());
            }
        });
    }

    private Product fetchProduct(String id) {
        String url = "https://tienda.mercadona.es/api/products/" + id;
        try {
            String json = httpClient.makeRequest(url);
            Product product = gson.fromJson(json, Product.class);
            buildCategories(product);
            addGluten(product);
            return sanitizeText(product);
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
        String text = product.getMandatoryMentions() != null ? product.getMandatoryMentions() : product.getDisplayName();
        text = text.toLowerCase();
        product.setGluten(text.contains("sin gluten") ? Boolean.TRUE : Boolean.FALSE);
    }

    private static Product sanitizeText(Product product) {
        if (product == null) return null;
        if (product.getDisplayName() != null) product.setDisplayName(cleanString(product.getDisplayName()));
        if (product.getCategory() != null) product.setCategory(cleanString(product.getCategory()));
        if (product.getSubcategory() != null) product.setSubcategory(cleanString(product.getSubcategory()));
        if (product.getSubsubcategory() != null) product.setSubsubcategory(cleanString(product.getSubsubcategory()));
        if (product.getBrand() != null) product.setBrand(cleanString(product.getBrand()));
        return product;
    }

    private static String cleanString(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String text = input.trim().toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        text = text.replaceAll("[^\\p{L}\\p{N}\\s]", "");
        text = text.replaceAll("\\s{2,}", " ").trim();
        return text;
    }
}