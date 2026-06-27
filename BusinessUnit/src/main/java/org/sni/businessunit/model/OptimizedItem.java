package org.sni.businessunit.model;

public record OptimizedItem(
        String userInput,
        Product jointBest,
        Product mercadonaBest,
        Product hiperdinoBest
) {}