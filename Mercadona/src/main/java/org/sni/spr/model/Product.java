package org.sni.spr.model;

import org.sni.spr.controller.CategoryPathBuilder;

import java.util.List;

public class Product {
    private int id;
    private String displayName;
    private String thumbnail;
    private List<CategoryNode> categories;
    private PriceInstructions priceInstructions;

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public List<CategoryNode> getCategories() {
        return categories;
    }

    public PriceInstructions getPriceInstructions() {
        return priceInstructions;
    }

    public String getFullCategory() {
        return CategoryPathBuilder.build(categories);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + getDisplayName() + '\'' +
                ", thumbnail='" + getThumbnail() + '\'' +
                ", category='" + getFullCategory() + '\'' +
                ", price=" + (priceInstructions != null ? priceInstructions.getUnitPrice() : null) +
                '}';
    }
}