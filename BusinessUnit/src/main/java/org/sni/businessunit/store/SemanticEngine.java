package org.sni.businessunit.store;

public interface SemanticEngine {
    float[] embed(String text);

    double computeRelevance(float[] vectorA, float[] vectorB);
}
