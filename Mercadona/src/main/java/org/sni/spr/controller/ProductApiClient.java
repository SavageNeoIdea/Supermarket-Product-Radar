package org.sni.spr.controller;


public class ProductApiClient {
    private final HttpClient httpClient;

    public ProductApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String fetchProductJson(String id) {
        String url = "https://tienda.mercadona.es/api/products/" + id;
        return httpClient.makeRequest(url);
    }
}