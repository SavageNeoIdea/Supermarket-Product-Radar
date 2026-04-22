package org.sni.spr.controller;

import org.sni.spr.model.*;
import java.util.*;


public class ProductService {

    private final ProductApiClient apiClient;
    private final ProductMapper mapper;

    public ProductService(ProductApiClient apiClient, ProductMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    public List<Product> getProducts(List<String> ids) {
        return ids.stream()
                .map(apiClient::fetchProductJson)
                .map(mapper::fromJson)
                .toList();
    }
}