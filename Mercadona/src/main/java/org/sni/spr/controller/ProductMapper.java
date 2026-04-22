package org.sni.spr.controller;

import org.sni.spr.model.*;
import com.google.gson.Gson;

public class ProductMapper {
    private final Gson gson = new Gson();

    public Product fromJson(String json) {
        try {
            return gson.fromJson(json, Product.class);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping product JSON", e);
        }
    }
}
