package org.sni.spr.controller;

import com.google.gson.*;
import org.sni.spr.model.Category;
import org.sni.spr.model.Product;

import java.util.*;

public class MercadonaFeeder {
    private static final Gson gson = new Gson();

    private final HttpClient httpClient = new HttpClient();
    private final CategoryParser categoryParser = new CategoryParser();
    private final ProductClient productClient = new ProductClient(httpClient);
    private final SitemapReader sitemapReader = new SitemapReader();

    private Map<Integer, Category> categoryMap = new HashMap<>();

    public void loadCategories() {
        String response = httpClient.makeRequest("https://tienda.mercadona.es/api/categories/");
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray results = json.getAsJsonArray("results");
        categoryMap = categoryParser.parse(results);
    }

    public List<String> readSitemap() {
        return sitemapReader.readSitemap("https://tienda.mercadona.es/sitemap.xml");
    }

    public Product getProduct(String id) {
        return productClient.getProduct(id);
    }

    public String extractId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 2];
    }

    public Map<Integer, Category> getCategoryMap() {
        return categoryMap;
    }
}