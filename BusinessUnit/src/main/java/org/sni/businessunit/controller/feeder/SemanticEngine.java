package org.sni.businessunit.controller.feeder;

public interface SemanticEngine {
    float[] embed(String text);
    double computeRelevance(float[] vectorA, float[] vectorB);
}
