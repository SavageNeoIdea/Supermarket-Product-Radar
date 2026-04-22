package org.sni.spr;

import org.sni.spr.controller.MercadonaFeeder;
import org.sni.spr.model.Product;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        MercadonaFeeder feeder = new MercadonaFeeder();
        feeder.loadCategories();
        List<String> urls = feeder.readSitemap().subList(1, 11);
        System.out.println("Total URLs: " + urls.size());
        urls
                .forEach(url -> {
                    String id = feeder.extractId(url);
                    Product product = feeder.getProduct(id);
                    if (product != null) {
                        System.out.println(product);
                    }
                });
    }
}