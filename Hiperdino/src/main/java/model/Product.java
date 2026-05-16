package model;

public final class Product {

    private final String name;
    private final double price;
    private final String measure;
    private final int quantity;
    private final int packageQuantity;
    private final String ean;
    private final String brand;
    private final String source;

    public Product(String name,
                   double price,
                   String measure,
                   int quantity,
                   int packageQuantity,
                   String ean,
                   String ProductBrand,
                   String ProductSource) {

        this.name = name;
        this.price = price;
        this.measure = measure;
        this.quantity = quantity;
        this.packageQuantity = packageQuantity;
        this.ean = ean;
        this.brand = ProductBrand;
        this.source = ProductSource;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getMeasure() {
        return measure;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPackageQuantity() {
        return packageQuantity;
    }

    public String getEan() {
        return ean;
    }

    public String getBrand() {
        return brand;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", measure='" + measure + '\'' +
                ", quantity=" + quantity +
                ", packageQuantity=" + packageQuantity +
                ", ean='" + ean + '\'' +
                ", brand=" + brand +
                ", source=" + source +
                '}';
    }
}