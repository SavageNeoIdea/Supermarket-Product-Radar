package org.sni.spr.controller;

import com.google.gson.*;
import org.sni.spr.model.Category;

import java.util.*;

public class CategoryParser {

    public Map<Integer, Category> parse(JsonArray results) {
        Map<Integer, Category> map = new HashMap<>();
        for (JsonElement el : results) {
            parseNode(el.getAsJsonObject(), null, map);
        }
        return map;
    }

    private void parseNode(JsonObject obj, Integer parentId, Map<Integer, Category> map) {
        int id = obj.get("id").getAsInt();
        String name = obj.get("name").getAsString();
        map.put(id, new Category(id, name, parentId));
        if (obj.has("categories")) {
            for (JsonElement sub : obj.getAsJsonArray("categories")) {
                parseNode(sub.getAsJsonObject(), id, map);
            }
        }
    }
}
