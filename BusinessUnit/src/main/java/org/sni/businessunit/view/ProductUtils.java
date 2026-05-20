package org.sni.businessunit.view;

import org.sni.businessunit.model.Product;
import org.sni.businessunit.model.UnitsOfMeasurement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductUtils {

    public static Map<String, Product> BestProductsForAnySources(Map<String, List<Product>> productInputMap) {
        Map<String, Product> inputToProductMap = new HashMap<>();
        for (Map.Entry<String, List<Product>> data : productInputMap.entrySet()){
            String inputKey = data.getKey();
            Product bestProduct = getProduct(data);
            if (bestProduct != null) {
                inputToProductMap.put(inputKey, bestProduct);
            }
        }
        return inputToProductMap;
    }

    private static Product getProduct(Map.Entry<String, List<Product>> data) {
        Product bestProduct = null;
        double bestProductScore = Double.MAX_VALUE;
        for (Product product : data.getValue()){
            double currentScore = calculateCompoundScore(product);
            if (currentScore < bestProductScore){
                bestProduct = product;
                bestProductScore = currentScore;
            }
        }
        return bestProduct;
    }

    public static double calculateCompoundScore(Product product){
        double pricePerSI = product.getPrice() / QuantitytoSI(product);
        double similarity = Math.max(0.01, product.getSimilarityScore());
        double similarityWeight = Math.pow(similarity, 3);
        return pricePerSI / similarityWeight;
    }

    public static double QuantitytoSI(Product product){
        UnitsOfMeasurement currentUnit = product.getMeasure();
        double currentQuantity = product.getQuantity();
        return currentQuantity * currentUnit.getFactorToSI();
    }
}