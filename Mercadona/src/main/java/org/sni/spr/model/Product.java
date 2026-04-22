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
    private List<CategoryNode> categories;

    @SerializedName("price_instructions")
    private PriceInstructions priceInstructions;


    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getThumbnail() { return thumbnail; }
    public String getBrand() { return brand; }
    public List<CategoryNode> getCategories() { return categories; }

    public double getUnitPrice() {
        return priceInstructions != null
                ? Double.parseDouble(priceInstructions.unitPrice)
                : 0.0;
    }

    public double getReferencePrice() {
        return priceInstructions != null
                ? Double.parseDouble(priceInstructions.referencePrice)
                : 0.0;
    }

    public double getUnitSize() {
        return priceInstructions != null
                ? priceInstructions.unitSize
                : 0.0;
    }

    public String getUnitName() {
        return priceInstructions != null
                ? priceInstructions.referenceFormat
                : null;
    }
}