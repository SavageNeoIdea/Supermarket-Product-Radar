package org.sni.spr;

import org.sni.spr.model.*;
import org.sni.spr.controller.*;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        String sitemapUrl = "https://tienda.mercadona.es/sitemap.xml";
        ProductProvider provider = new MercadonaProductProvider();
        HttpClientManager httpClient = new MercadonaHttpClient();
        MercadonaProductService productService = new MercadonaProductService(httpClient);


        List<String> ids = provider.provideProductIDs(sitemapUrl);
        System.out.println("IDs obtenidos: " + ids.size());
        ids = ids.subList(0,10);
        List<Product> products = productService.getProducts(ids);
        System.out.println("Productos obtenidos: " + products.size());
        for (Product p : products) {
            System.out.println(p.getDisplayName());
            System.out.println(p.getCategory());
            System.out.println(p.getSubcategory());
            System.out.println(p.getSubsubcategory());
            System.out.println(p.getUnitPrice());
            System.out.println("---------------");
        }
    }
}