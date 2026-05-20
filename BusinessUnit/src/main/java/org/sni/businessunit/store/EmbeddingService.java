package org.sni.businessunit.store;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import java.util.Objects;

public final class EmbeddingService implements SemanticEngine{

    private static final int DEFAULT_VECTOR_DIMENSION = 384;
    private final EmbeddingModel embeddingModel;

    public EmbeddingService() {
        this(new AllMiniLmL6V2QuantizedEmbeddingModel());
    }

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel no puede ser null");
    }

    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[DEFAULT_VECTOR_DIMENSION];
        }
        Embedding embedding = embeddingModel.embed(text).content();
        return embedding.vector();
    }

    public double computeRelevance(float[] vectorA, float[] vectorB) {
        double cosine = computeCosineSimilarity(vectorA, vectorB);
        return dev.langchain4j.store.embedding.RelevanceScore.fromCosineSimilarity(cosine);
    }

    private double computeCosineSimilarity(float[] vectorA, float[] vectorB) {
        Objects.requireNonNull(vectorA, "vectorA no puede ser null");
        Objects.requireNonNull(vectorB, "vectorB no puede ser null");
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Los vectores deben tener la misma longitud");
        }
        double dotProduct = 0.0;
        double normASq = 0.0;
        double normBSq = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += (double) vectorA[i] * vectorB[i];
            normASq += (double) vectorA[i] * vectorA[i];
            normBSq += (double) vectorB[i] * vectorB[i];
        }

        if (normASq == 0.0 || normBSq == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normASq) * Math.sqrt(normBSq));
    }
}
