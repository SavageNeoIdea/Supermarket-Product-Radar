package org.sni.spr.model;

import java.util.List;

public class Product {
    private int id;
    private String ean;
    private String displayName;
    private String thumbnail;
    private String brand;
    private List<CategoryNode> categories;
    private int quantity;
    private int packageqty;
    private String measure;
    private boolean gluten;
    private double unitPrice;
    private double referencePrice;
    private String unitName;
    private double unitSize;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", ean='" + ean + '\'' +
                ", displayName='" + displayName + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", brand='" + brand + '\'' +
                ", categories=" + categories + '\'' +
                ", quantity=" + quantity + '\'' +
                ", packageQty=" + packageqty +
                ", measure='" + measure + '\'' +
                ", gluten=" + gluten + '\'' +
                ", unitPrice=" + unitPrice +
                '}';
    }

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

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getReferencePrice() {
        return referencePrice;
    }

    public String getUnitName() {
        return unitName;
    }

    public double getUnitSize() {
        return unitSize;
    }

    public String getEan() {
        return ean;
    }

    public String getBrand() {
        return brand;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPackageqty() {
        return packageqty;
    }

    public String getMeasure() {
        return measure;
    }

    public boolean isGluten() {
        return gluten;
    }
}