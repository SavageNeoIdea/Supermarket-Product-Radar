package org.sni.businessunit.controller.embedding;
import com.google.gson.Gson;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.RelevanceScore;
import org.sni.businessunit.model.Product;
import java.util.Objects;

public final class EmbeddingService implements SemanticEngine {
    private static final double SIMILARITY_THRESHOLD = 0.45;
    private static final double EXACT_MATCH_BONUS = 0.20;
    private final EmbeddingModel embeddingModel;
    private Gson gson;
    public EmbeddingService() {
        this(new AllMiniLmL6V2QuantizedEmbeddingModel());
        this.gson = new Gson();
    }

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel no puede ser null");
        this.gson = new Gson();
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

    @Override
    public Product scoreProductHybrid(Product product, float[] queryVector, String queryText) {
        try {
            float[] productVector = gson.fromJson(product.getEmbeddingVector(), float[].class);
            double semanticScore = computeRelevance(queryVector, productVector);
            if (semanticScore > SIMILARITY_THRESHOLD) {
                double finalScore = applyLexicalBonus(semanticScore, product.getName().toLowerCase(), queryText);
                product.setSimilarityScore(finalScore);
                return product;
            }
        } catch (Exception e) {
            System.err.printf("Error calculando similitud para producto [%s]: %s%n", product.getName(), e.getMessage());
        }
        return null;
    }

    private double applyLexicalBonus(double semanticScore, String productName, String queryText) {
        String singularQuery = queryText.endsWith("s") ? queryText.substring(0, queryText.length() - 1) : queryText;
        if (productName.contains(queryText) || productName.contains(singularQuery)) {
            return semanticScore + EXACT_MATCH_BONUS;
        }
        return semanticScore;
    }
}