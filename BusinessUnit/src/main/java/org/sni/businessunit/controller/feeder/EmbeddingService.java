package org.sni.businessunit.controller.feeder;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.RelevanceScore;

import java.util.Objects;

public final class EmbeddingService implements SemanticEngine {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService() {
        this(new AllMiniLmL6V2QuantizedEmbeddingModel());
    }

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel no puede ser null");
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            int dimension = embeddingModel.embed("dimension_check").content().dimension();
            return new float[dimension];
        }
        return embeddingModel.embed(text).content().vector();
    }
    @Override
    public double computeRelevance(float[] vectorA, float[] vectorB) {
        Objects.requireNonNull(vectorA, "vectorA no puede ser null");
        Objects.requireNonNull(vectorB, "vectorB no puede ser null");
        Embedding embeddingA = Embedding.from(vectorA);
        Embedding embeddingB = Embedding.from(vectorB);

        double cosine = CosineSimilarity.between(embeddingA, embeddingB);
        return RelevanceScore.fromCosineSimilarity(cosine);
    }
}