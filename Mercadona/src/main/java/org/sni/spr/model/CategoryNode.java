package org.sni.spr.model;

import java.util.List;

public class CategoryNode {
    private int id;
    private String name;
    private List<CategoryNode> categories;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CategoryNode> getCategories() {
        return categories;
    }
}