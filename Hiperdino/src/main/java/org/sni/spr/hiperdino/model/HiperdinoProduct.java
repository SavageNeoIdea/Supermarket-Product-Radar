package org.sni.spr.hiperdino.model;

import java.time.LocalDateTime;

public class HiperdinoProduct {

    private final String sku;
    private final String ean;
    private final String brand;
    private final String category;
    private final String subcategory;
    private final String name;
    private final int packageQty;
    private final int qty;
    private final UnitsOfMeasurement measure;
    private final double price;
    private final Boolean gluten;
    private final String urlImage;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public HiperdinoProduct(String sku, String ean, String brand,
                            String category, String subcategory, String name,
                            int qty, int packageQty, UnitsOfMeasurement measure,
                            double price, boolean gluten, String urlImage) {
        this.sku = sku;
        this.ean = ean;
        this.brand = brand;
        this.category = category;
        this.subcategory = subcategory;
        this.name = name;
        this.qty = qty;
        this.packageQty = packageQty;
        this.measure = measure;
        this.price = price;
        this.gluten = gluten;
        this.urlImage = urlImage;
    }


    @Override
    public String toString() {
        return "HiperdinoProduct{" +
                "sku='" + sku + '\'' +
                ", ean='" + ean + '\'' +
                ", brand='" + brand + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", qty=" + qty +
                " " + measure +
                ", gluten=" + gluten +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getSku() {return sku;}
    public String getEan() {return ean;}
    public String getBrand() {return brand;}
    public String getCategory() {return category;}
    public String getSubcategory() {return subcategory;}
    public String getName() {return name;}
    public int getQty() {return qty;}
    public int getPackageQty() {return packageQty;}
    public String getMeasure() {return measure.name();}
    public double getPrice() {return price;}
    public boolean isGluten() {return gluten;}
    public String getUrlImage() {return urlImage;}
    public LocalDateTime getTimestamp() {return timestamp;}
}