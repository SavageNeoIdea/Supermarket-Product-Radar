package org.sni.spr.hiperdino.controller;

import org.sni.spr.hiperdino.model.Product;
import org.sni.spr.hiperdino.view.ProductFeeder;
import org.sni.spr.hiperdino.view.WebScraper;

import java.util.List;
import java.util.Map;

public class Controller {
    private final WebScraper webScraper;
    private final ProductFeeder productFeeder;
    private final String postalCode;
    private final Storer storer;

    public Controller(ProductFeeder productFeeder, WebScraper webScraper, String postalCode, Storer storer) {
        this.postalCode = postalCode;
        this.productFeeder = productFeeder;
        this.webScraper = webScraper;
        this.storer = storer;
    }

    public void init() {
        webScraper.init(postalCode);
        Map<String, List<Map<String, String>>> rawData = webScraper.extractProductRawData();
        Map<String, List<Product>> productsMap = productFeeder.getProducts(rawData);
        storer.storeAllData(productsMap);
        webScraper.close();
    }
}