package org.sni.spr.hiperdino;

import java.time.LocalDateTime;

public class Product {

    private static int nextId = 1;

    private final int id;
    private final String name;
    private final String qty;
    private final String price;
    private final Boolean gluten;
    private final Boolean gourmet;
    private final LocalDateTime now = LocalDateTime.now();

    public Product(String name, String qty, String price, boolean gluten, boolean gourmet, int id){

        this.id = nextId++;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.gluten = gluten;
        this.gourmet = gourmet;
    }
}

