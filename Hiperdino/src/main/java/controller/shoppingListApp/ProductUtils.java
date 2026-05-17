package controller.shoppingListApp;

import model.Product;
import model.UnitsOfMeasurement;

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
        double bestProductScore = 9999;
        for (Product product : data.getValue()){
            double newDistance = getProductDistance(product);
            if (newDistance < bestProductScore){
                bestProduct = product;
                bestProductScore = newDistance;
            }
        }
        return bestProduct;
    }

    public static double getProductDistance(Product product){
        return product.getPrice() / QuantitytoSI(product);
    }

    public static double QuantitytoSI(Product product){
        UnitsOfMeasurement currentUnit = product.getMeasure();
        double currentQuantity = product.getQuantity();
        return currentQuantity * currentUnit.getFactorToSI();
    }


}