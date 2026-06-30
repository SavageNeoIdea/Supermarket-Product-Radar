package org.sni.businessunit.model;
import java.util.List;

public record OptimizedItem(
        String userInput,
        List<Product> jointBestList,
        List<Product> mercadonaBestList,
        List<Product> hiperdinoBestList
) {}