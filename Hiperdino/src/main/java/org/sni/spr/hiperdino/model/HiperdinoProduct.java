package org.sni.spr.hiperdino.model;



import java.time.LocalDateTime;



public class HiperdinoProduct implements Product{

    private final String category;
    private final String subcategory;
    private final String name;
    private final int packageQty;
    private final int qty;
    private final UnitsOfMeasurement measure;
    private final double price;
    private final Boolean gluten;
    private final LocalDateTime now = LocalDateTime.now();
    private final String urlImage;

    public HiperdinoProduct(String category, String subCategory, String name, int qty, int packageQty,
                   UnitsOfMeasurement measure, double price, boolean gluten, String urlImage){
        this.category = category;
        this.subcategory = subCategory;
        this.name = name;
        this.price = price;
        this.packageQty = packageQty;
        this.qty = qty;
        this.measure = measure;
        this.gluten = gluten;
        this.urlImage = urlImage;
    }

    @Override
    public String getCategory() {return category;}
    @Override
    public String getSubcategory() {return subcategory;}
    @Override
    public String getName() { return name; }
    @Override
    public double getPrice() { return this.price; }
    @Override
    public int getPackageQty() { return packageQty; }
    @Override
    public int getQty() { return qty; }
    @Override
    public UnitsOfMeasurement getMeasure() { return measure; }
    @Override
    public Boolean getGluten() {return gluten;}
    @Override
    public LocalDateTime getNow() {return now;}
    @Override
    public String getUrlImage() {return urlImage;}

    @Override
    public String toString() {
        return "Product{" +
                ", category='" + category + "'" +
                ", subcategory='" + subcategory + "'" +
                ", name='" + name + "'" +
                ", qty=" + qty +
                ", packageQty=" + packageQty +
                ", measure='" + measure + "'" +
                ", price=" + price +
                ", gluten=" + gluten +
                ", urlImage='" + urlImage + "'" +
                '}';

    }
}