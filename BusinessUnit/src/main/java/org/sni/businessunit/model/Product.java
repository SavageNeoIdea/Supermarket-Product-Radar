package org.sni.businessunit.model;
import com.google.gson.Gson;
import org.sni.businessunit.controller.embedding.SemanticEngine;

public class Product {
    private final String name;
    private final double price;
    private final UnitsOfMeasurement measure;
    private final int quantity;
    private final int packageQuantity;
    private final String ean;
    private final String brand;
    private final String source;
    private final String ts;
    private String embeddingVector;
    private double similarityScore;

    public Product(String name, double price, String measure, int quantity, int packageQuantity, String ean, String brand, String source, String ts) {
        this.name = name;
        this.price = price;
        this.measure = UnitsOfMeasurement.valueOf(measure);
        this.quantity = quantity;
        this.packageQuantity = packageQuantity;
        this.ean = ean;
        this.brand = brand;
        this.source = source;
        this.ts = ts;
        this.embeddingVector = null;
        this.similarityScore = 1.0;
    }

    public Product(String name, double price, String measure, int quantity, int packageQuantity, String ean, String brand, String source, String ts, String embeddingVector) {
        this.name = name;
        this.price = price;
        this.measure = UnitsOfMeasurement.valueOf(measure);
        this.quantity = quantity;
        this.packageQuantity = packageQuantity;
        this.ean = ean;
        this.brand = brand;
        this.source = source;
        this.ts = ts;
        this.embeddingVector = embeddingVector;
        this.similarityScore = 1.0;
    }

    public void generateEmbedding(SemanticEngine iaService, Gson gson) {
        if (this.name != null && !this.name.isBlank()) {
            float[] vector = iaService.embed(this.name);
            this.embeddingVector = gson.toJson(vector);
        }
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public UnitsOfMeasurement getMeasure() { return measure; }
    public int getQuantity() { return quantity; }
    public int getPackageQuantity() { return packageQuantity; }
    public String getEan() { return ean; }
    public String getBrand() { return brand; }
    public String getSource() { return source; }
    public String getTs() { return ts; }
    public String getEmbeddingVector() { return embeddingVector; }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", measure=" + measure +
                ", quantity=" + quantity +
                ", packageQuantity=" + packageQuantity +
                ", ean='" + ean + '\'' +
                ", brand='" + brand + '\'' +
                ", source='" + source + '\'' +
                ", ts='" + ts + '\'' +
                ", similarityScore=" + similarityScore +
                '}';
    }
}