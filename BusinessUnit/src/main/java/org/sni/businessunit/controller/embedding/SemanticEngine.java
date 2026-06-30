package org.sni.businessunit.controller.embedding;

import org.sni.businessunit.model.Product;

public interface SemanticEngine {
    float[] embedInput(String text);
    double computeRelevance(float[] vectorA, float[] vectorB);
    String embedToString(String embeddingVector);
    Double calculateMatchScore(Product productText, float[] queryInput, String queryText);
}
