package org.sni.spr;

import org.sni.spr.model.*;
import org.sni.spr.controller.*;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        String sitemapUrl = "https://tienda.mercadona.es/sitemap.xml";
        ProductProvider provider = new MercadonaProductProvider();
        HttpClient httpClient = new MercadonaHttpClient();
        ProductApiClient apiClient = new ProductApiClient(httpClient);
        ProductMapper mapper = new ProductMapper();
        ProductService productService = new ProductService(apiClient, mapper);


        List<String> ids = provider.provideProductIDs(sitemapUrl);
        System.out.println("IDs obtenidos: " + ids.size());
        ids = ids.subList(0,10);
        List<Product> products = productService.getProducts(ids);
        System.out.println("Productos obtenidos: " + products.size());
        for (Product p : products) {
            System.out.println(p.getDisplayName());
            System.out.println(p.getThumbnail());
            System.out.println(p.getCategories());
            System.out.println(p.getUnitPrice());
        }
    }
}