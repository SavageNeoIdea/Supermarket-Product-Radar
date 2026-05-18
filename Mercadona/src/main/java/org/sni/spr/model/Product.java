package org.sni.spr.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;


public class Product {
    private float id;
    private String ean;

    @SerializedName("display_name")
    private String displayName;

    private String thumbnail;
    private String brand;
    private List<Category> categories;
    private String category;
    private String subcategory;
    private String subsubcategory;
    private Details details;
    private Boolean gluten;

    @SerializedName("price_instructions")
    private PriceInstructions priceInstructions;


    public float getId() { return id; }
    public String getEan() { return ean; }
    public String getDisplayName() { return displayName; }
    public String getThumbnail() { return thumbnail; }
    public String getBrand() { return brand; }
    public List<Category> getCategories() { return categories; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getSubsubcategory() { return subsubcategory; }
    public String getMandatoryMentions() { return details != null ? details.getMandatoryMentions() : null; }
    public Boolean getGluten() { return gluten; }

    public double getUnitPrice() {
        return priceInstructions != null ? Double.parseDouble(priceInstructions.unitPrice) : 0.0;
    }
    public double getUnitSize() {
        return priceInstructions != null ? priceInstructions.unitSize : 0.0;
    }
    public String getUnitType() {
        return priceInstructions != null ? priceInstructions.sizeFormat : null;
    }
    public int getTotalUnits() {
        return priceInstructions != null ? priceInstructions.totalUnits : 0;
    }


    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public void setSubsubcategory(String subsubcategory) { this.subsubcategory = subsubcategory; }
    public void setGluten(Boolean gluten) { this.gluten = gluten; }
}