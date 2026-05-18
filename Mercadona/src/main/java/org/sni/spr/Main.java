package org.sni.spr;

import org.sni.spr.controller.*;
import org.sni.spr.store.*;

import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {
        String sitemapUrl = "https://tienda.mercadona.es/sitemap.xml";
        ProductProvider provider = new MercadonaProductProvider();
        HttpClientManager httpClient = new MercadonaHttpClient();
        ProductService productService = new MercadonaProductService(httpClient);
        try (Storer storer = new ActiveMQStorer()) {
            Controller controller = new Controller(provider, productService, storer);
            controller.run(sitemapUrl);
            controller.scheduleDailyRun(sitemapUrl, LocalTime.of(3, 0));
        }
    }
}