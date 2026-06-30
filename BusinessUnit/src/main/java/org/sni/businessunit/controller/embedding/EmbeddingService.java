package org.sni.businessunit.controller.embedding;
import com.google.gson.Gson;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.RelevanceScore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public final class EmbeddingService implements SemanticEngine {
    private static final double SIMILARITY_THRESHOLD = 0.75;
    private static final double MAX_LEXICAL_BONUS     = 0.12;
    private static final double QUERY_COVERAGE_WEIGHT = 0.70;
    private static final double JACCARD_WEIGHT        = 0.30;
    private static final Set<String> STOPWORDS = Set.of(
            "de", "la", "el", "en", "con", "del", "los", "las", "para",
            "por", "sin", "una", "uno", "que", "sus", "cada"
    );

    private final EmbeddingModel embeddingModel;
    private final Gson gson;


    public EmbeddingService() {
        this(buildMultilingualModel());
    }

    private static EmbeddingModel buildMultilingualModel() {
        try {
            Path modelPath = Paths.get(
                    Objects.requireNonNull(EmbeddingService.class.getResource("/model_quantized.onnx")).toURI()
            );
            Path tokenizerPath = Paths.get(
                    Objects.requireNonNull(EmbeddingService.class.getResource("/tokenizer.json")).toURI()
            );
            return new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el modelo multilingüe", e);
        }
    }

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel no puede ser null");
        this.gson = new Gson();
    }

    @Override
    public String embedToString(String text) {
        return gson.toJson(calculateEmbeddingVector(text));
    }

    @Override
    public float[] embedInput(String text) {
        return calculateEmbeddingVector(sanitizeText(text));
    }

    private float[] calculateEmbeddingVector(String text) {
        if (textIsNotValid(text)) {
            int dimension = embeddingModel.embed("dimension_check").content().dimension();
            return new float[dimension];
        }
        return embeddingModel.embed(text).content().vector();
    }

    private static String sanitizeText(String input) {
        if (textIsNotValid(input)) {
            return "";
        }
        String text = input.trim().toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        text = text.replaceAll("[^\\p{L}\\p{N}\\s]", "");
        text = text.replaceAll("\\s{2,}", " ").trim();
        return text;
    }

    private static boolean textIsNotValid(String text) {
        return text == null || text.isBlank();
    }

    @Override
    public double computeRelevance(float[] vectorA, float[] vectorB) {
        Embedding embeddingA = Embedding.from(vectorA);
        Embedding embeddingB = Embedding.from(vectorB);
        double cosine = CosineSimilarity.between(embeddingA, embeddingB);
        return RelevanceScore.fromCosineSimilarity(cosine);
    }

    @Override
    public Double calculateHybridScore(float[] productVector, String productText, float[] queryVector, String queryText) {
        try {
            double semanticScore = computeRelevance(queryVector, productVector);
            if (semanticScore > SIMILARITY_THRESHOLD) {
                return applyLexicalBonus(semanticScore, productText, queryText);
            }
        } catch (Exception e) {
            System.err.printf("Error calculando similitud semántica para el texto [%s]: %s%n", productText, e.getMessage());
        }
        return null;
    }

    private double applyLexicalBonus(double semanticScore, String productName, String queryText) {
        Set<String> queryTokens   = tokenize(queryText);
        Set<String> productTokens = tokenize(productName);

        if (queryTokens.isEmpty() || productTokens.isEmpty()) {
            return semanticScore;
        }

        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(productTokens);
        int commonCount = intersection.size();

        if (commonCount == 0) {
            return semanticScore;
        }
        double queryCoverage = (double) commonCount / queryTokens.size();
        Set<String> union = new HashSet<>(queryTokens);
        union.addAll(productTokens);
        double jaccard = (double) commonCount / union.size();
        double lexicalScore = (queryCoverage * QUERY_COVERAGE_WEIGHT)
                + (jaccard       * JACCARD_WEIGHT);

        double bonus = lexicalScore * MAX_LEXICAL_BONUS;

        return semanticScore + bonus;
    }

    private Set<String> tokenize(String text) {
        String normalized = sanitizeText(text);
        if (normalized.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOPWORDS.contains(w))
                .map(this::extractRoot)
                .filter(r -> r != null && r.length() > 1)
                .collect(Collectors.toSet());
    }

    private String extractRoot(String word) {
        if (word == null || word.length() <= 3) {
            return word;
        }
        String root = word;
        if (root.endsWith("es") && root.length() > 4) {
            root = root.substring(0, root.length() - 2);
        } else if (root.endsWith("s")) {
            root = root.substring(0, root.length() - 1);
        }
        if (root.endsWith("o") || root.endsWith("a")) {
            root = root.substring(0, root.length() - 1);
        }
        return root;
    }
}