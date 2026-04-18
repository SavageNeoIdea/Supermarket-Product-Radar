package org.sni.spr;

import com.google.gson.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.*;


public class MercadonaFeeder {
    private static final Gson gson = new Gson();
    public static final Map<Integer, Category> categoryMap = new HashMap<>();


    public static void main(String[] args) throws Exception {
        loadCategories();
        List<String> urls = readSitemap("https://tienda.mercadona.es/sitemap.xml").subList(0,10);
        System.out.println("Total URLs: " + urls.size());
        for (String url : urls) {
            if (!url.contains("/product/")) continue;
            String id = extractId(url);
            Product product = getProduct(id);
            if (product != null) {
                System.out.println(product);
            }
        }
    }

    private static void loadCategories() {
        String response = makeRequest("https://tienda.mercadona.es/api/categories/");
        if (response == null) return;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray results = json.getAsJsonArray("results");
        for (JsonElement el : results) {
            parseCategory(el.getAsJsonObject(), null);
        }
    }

    private static void parseCategory(JsonObject obj, Integer parentId) {
        int id = obj.get("id").getAsInt();
        String name = obj.get("name").getAsString();
        categoryMap.put(id, new Category(id, name, parentId));
        if (obj.has("categories")) {
            for (JsonElement sub : obj.getAsJsonArray("categories")) {
                parseCategory(sub.getAsJsonObject(), id);
            }
        }
    }


    public static Product getProduct(String id) {
        String url = "https://tienda.mercadona.es/api/products/" + id;
        String response = makeRequest(url);
        if (response == null) return null;
        try {
            return gson.fromJson(response, Product.class);
        } catch (Exception e) {
            return null;
        }
    }


    private static List<String> readSitemap(String sitemapUrl) throws Exception {
        List<String> urls = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new URI(sitemapUrl).toURL().openStream());
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT &&
                    "loc".equals(reader.getLocalName())) {
                urls.add(reader.getElementText().trim());
            }
        }
        return urls;
    }


    private static String extractId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 2];
    }


    private static String makeRequest(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URI(urlString).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            conn.disconnect();
        } catch (Exception e) {
            return null;
        }
        return result.toString();
    }
}