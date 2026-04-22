package org.sni.spr.controller;

import com.google.gson.*;
import org.sni.spr.model.*;
import org.sni.spr.*;

import java.util.*;

public class MercadonaFeeder {
    private static final Gson gson = new Gson();

    private final CategoryUtils categoryUtils = new CategoryUtils();
    private final ProductClient productClient = new ProductClient();
    private final ProductProvider sitemapReader = new MercadonaProductProvider();
    private final HttpClient httpClient = new NetHttpClient();
    private Map<Integer, Category> categoryMap = new HashMap<>();

    public void loadCategories() {
        String response = httpClient.makeRequest("https://tienda.mercadona.es/api/categories/");
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray results = json.getAsJsonArray("results");
        categoryMap = categoryUtils.parse(results);
    }

    public List<String> readSitemap() {
        return sitemapReader.provideProductIDs("https://tienda.mercadona.es/sitemap.xml");
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