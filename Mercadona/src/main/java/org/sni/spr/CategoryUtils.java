package org.sni.spr;

import com.google.gson.*;
import org.sni.spr.model.Category;
import org.sni.spr.model.CategoryNode;

import java.util.*;

public class CategoryUtils {

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


    public String buildPath(List<CategoryNode> categories) {
        if (categories == null || categories.isEmpty()) return null;
        List<String> path = new ArrayList<>();
        CategoryNode node = categories.getFirst();
        while (node != null) {
            path.add(node.getName());
            if (node.getCategories() == null || node.getCategories().isEmpty()) {
                break;
            }
            node = node.getCategories().getFirst();
        }
        Collections.reverse(path);
        return String.join(" > ", path);
    }
}
