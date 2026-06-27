package org.sni.businessunit.controller.embedding;

import org.sni.businessunit.model.Product;

public interface SemanticEngine {
    float[] embed(String text);
    double computeRelevance(float[] vectorA, float[] vectorB);
    Product scoreProductHybrid(Product product, float[] queryVector, String queryText);
}
