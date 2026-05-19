package org.sni.spr.mercadona.model;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private List<Category> categories;

    public String getName() { return name; }
    public List<Category> getCategories() { return categories; }


    public List<String> extractPath() {
        List<String> path = new ArrayList<>();
        traverse(this, path);
        return path;
    }


    private void traverse(Category node, List<String> path) {
        if (node == null) return;
        path.add(node.getName());
        if (node.getCategories() != null && !node.getCategories().isEmpty()) {
            traverse(node.getCategories().getFirst(), path);
        }
    }
}