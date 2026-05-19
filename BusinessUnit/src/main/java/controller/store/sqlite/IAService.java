package controller.store.sqlite;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;

public class IAService {

    private final EmbeddingModel model = new AllMiniLmL6V2QuantizedEmbeddingModel();
    public float[] obtainVector(String texto) {
        if (texto == null || texto.isBlank()) {
            return new float[384];
        }
        Embedding embedding = model.embed(texto).content();
        return embedding.vector();
    }

    public double calculateSimilitude(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Los vectores deben tener la misma longitud");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        double cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return dev.langchain4j.store.embedding.RelevanceScore.fromCosineSimilarity(cosineSimilarity);
    }
}