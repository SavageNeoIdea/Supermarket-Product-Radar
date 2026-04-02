package org.sni.spr.hiperdino;

import java.time.LocalDateTime;

public class Product {

    private static int nextId = 1;

    private final int id;
    private final String category;
    private final String subcategory;
    private final String name;
    private final int qty;
    private final UnitsOfMeasurement measure;
    private final double price;
    private final Boolean gluten;
    private final LocalDateTime now = LocalDateTime.now();
    private final String urlImage;

    public Product(String category, String subCategory, String name, int qty, UnitsOfMeasurement measure, double price,
                   boolean gluten, String urlImage){

        this.id = nextId++;
        this.category = category;
        this.subcategory = subCategory;
        this.name = name;
        this.qty = qty;
        this.measure = measure;
        this.price = price;
        this.gluten = gluten;
        this.urlImage = urlImage;
    }
}

