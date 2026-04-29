package org.sni.spr.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;


public class Product {
    private int id;
    private String ean;

    @SerializedName("display_name")
    private String displayName;

    private String thumbnail;
    private String brand;
    private List<Category> categories;
    private String category;
    private String subcategory;
    private String subsubcategory;

    @SerializedName("price_instructions")
    private PriceInstructions priceInstructions;


    public void buildCategories() {
        if (categories == null || categories.isEmpty()) return;
        List<String> path = categories.getFirst().extractPath();
        this.category = !path.isEmpty() ? path.get(0) : null;
        this.subcategory = path.size() > 1 ? path.get(1) : null;
        this.subsubcategory = path.size() > 2 ? path.get(2) : null;
    }

    public int getId() { return id; }
    public String getEan() { return ean; }
    public String getDisplayName() { return displayName; }
    public String getThumbnail() { return thumbnail; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getSubsubcategory() { return subsubcategory; }

    public double getUnitPrice() {
        return priceInstructions != null ? Double.parseDouble(priceInstructions.unitPrice) : 0.0;
    }
    public double getReferencePrice() {
        return priceInstructions != null ? Double.parseDouble(priceInstructions.referencePrice) : 0.0;
    }
    public double getUnitSize() {
        return priceInstructions != null ? priceInstructions.unitSize : 0.0;
    }
    public String getUnitName() {
        return priceInstructions != null ? priceInstructions.referenceFormat : null;
    }
}