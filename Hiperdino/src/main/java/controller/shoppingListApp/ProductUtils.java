package controller.shoppingListApp;

import model.Product;
import model.UnitsOfMeasurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductUtils {

    public static List<Map<String, Product>> BestProductsForAllSources(Map<String, List<Product>> productInputMap) {
        List<Map<String, Product>> result = new ArrayList<>();

        for (Map.Entry<String, List<Product>> data : productInputMap.entrySet()){
            String inputKey = data.getKey();
            Product bestProduct = null;
            double bestProductScore = 9999;

            for (Product product : data.getValue()){
                double newDistance = getProductDistance(product);
                if (newDistance < bestProductScore){
                    bestProduct = product;
                    bestProductScore = newDistance;
                }
            }

            if (bestProduct != null) {
                Map<String, Product> inputToProductMap = new HashMap<>();
                inputToProductMap.put(inputKey, bestProduct);
                result.add(inputToProductMap);
            }
        }
        return result;
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