package org.sni.businessunit.controller.embedding;

public interface SemanticEngine {
    float[] embedInput(String text);
    double computeRelevance(float[] vectorA, float[] vectorB);
    String embedToString(String embeddingVector);
    Double calculateHybridScore(float[] productVector, String productText, float[] queryVector, String queryText);
}
