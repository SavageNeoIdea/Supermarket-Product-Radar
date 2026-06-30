package org.sni.businessunit.model;

public class Product {
    private final String name;
    private final double price;
    private UnitsOfMeasurement measure;
    private double quantity;
    private final int packageQuantity;
    private final String ean;
    private final String brand;
    private final String source;
    private final String ts;
    private String embeddingVector;
    private double similarityScore;

    public Product(String name, double price, String measure, double quantity, int packageQuantity, String ean, String brand, String source, String ts) {
        this.name = name;
        this.price = price;
        this.measure = UnitsOfMeasurement.valueOf(measure);
        this.quantity = quantity;
        this.packageQuantity = packageQuantity == 0 ? 1 : packageQuantity;
        this.ean = ean;
        this.brand = brand;
        this.source = source;
        this.ts = ts;
        this.embeddingVector = null;
        this.similarityScore = 1.0;
        normalizeMeasureAndQuantity();
    }

    public Product(String name, double price, String measure, double quantity, int packageQuantity, String ean, String brand, String source, String ts, String embeddingVector) {
        this.name = name;
        this.price = price;
        this.measure = UnitsOfMeasurement.valueOf(measure);
        this.quantity = quantity;
        this.packageQuantity = packageQuantity == 0 ? 1 : packageQuantity;
        this.ean = ean;
        this.brand = brand;
        this.source = source;
        this.ts = ts;
        this.embeddingVector = embeddingVector;
        this.similarityScore = 1.0;
        normalizeMeasureAndQuantity();
    }

    private void normalizeMeasureAndQuantity() {
        if (this.measure == null) return;
        double factor = this.measure.getFactorToSI();
        if (factor != 1.0) {
            this.quantity = this.quantity * factor;
            this.measure = findBaseUnit(this.measure.getMagnitude());
        }
    }

    private UnitsOfMeasurement findBaseUnit(UnitsOfMeasurement.Magnitude magnitude) {
        for (UnitsOfMeasurement u : UnitsOfMeasurement.values()) {
            if (u.getMagnitude() == magnitude && u.getFactorToSI() == 1.0) {
                return u;
            }
        }
        return this.measure;
    }

    public String getEmbeddingText(){
        String base = this.name;

        if (this.brand != null && !this.brand.isBlank()
                && !this.name.contains(this.brand)) {
            base = base + " " + this.brand;
        }
        return base.trim();
    }

    public double getSimilarityScore() { return similarityScore; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public UnitsOfMeasurement getMeasure() { return measure; }
    public double getQuantity() { return quantity; }
    public int getPackageQuantity() { return packageQuantity; }
    public String getEan() { return ean; }
    public String getBrand() { return brand; }
    public String getSource() { return source; }
    public String getTs() { return ts; }
    public String getEmbeddingVector() { return embeddingVector; }

    public void setEmbeddingVector(String embeddingVector) {
        this.embeddingVector = embeddingVector;
    }

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