package org.sni.businessunit.view;

import org.sni.businessunit.model.Product;

import java.util.*;
import java.util.stream.Collectors;

public class ShoppingListBuilder {
    public Map<String, Map<String, List<Product>>> productInputSourceMap;
    public Map<String, List<Product>> productInputMap;

    public ShoppingListBuilder() {
        this.productInputMap = new LinkedHashMap<>();
    }

    public String buildShopList() {
        List<Map<String, Product>> shopListData = buildShopListData();
        if (shopListData == null || shopListData.size() < 3) {
            return "Error: No hay suficientes datos para calcular las listas de la compra.";
        }

        Map<String, Product> jointMap = shopListData.get(0);
        Map<String, Product> mercadonaMap = shopListData.get(1);
        Map<String, Product> hiperdinoMap = shopListData.get(2);

        StringBuilder sb = new StringBuilder();

        sb.append("====================================================================\n");
        sb.append("🛒            LISTA DE LA COMPRA CONJUNTA OPTIMIZADA               \n");
        sb.append("====================================================================\n");

        double jointTotal = 0.0;

        for (Product p : jointMap.values()) {
            if (p != null) {
                sb.append(String.format("- [%s] %s: %.2f€ | Cantidad: %d (%s)\n",
                        p.getSource().toUpperCase(),
                        p.getName(),
                        p.getPrice(),
                        p.getQuantity(),
                        p.getMeasure()));

                jointTotal += p.getPrice();
            }
        }

        sb.append("--------------------------------------------------------------------\n");
        sb.append(String.format("💰 COSTE TOTAL OPTIMIZADO (Yendo a ambos): %.2f€\n", jointTotal));
        sb.append("====================================================================\n\n");

        double mercadonaTotal = mercadonaMap.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Product::getPrice)
                .sum();

        double hiperdinoTotal = hiperdinoMap.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Product::getPrice)
                .sum();

        double lossIfMercadona = mercadonaTotal - jointTotal;
        double lossIfHiperdino = hiperdinoTotal - jointTotal;

        sb.append("====================================================================\n");
        sb.append("📊               ANÁLISIS DE PÉRDIDAS POR COMPRAR EN UN SOLO SITIO   \n");
        sb.append("====================================================================\n");

        sb.append(String.format("▶️ SI COMPRAS TODO EN MERCADONA:\n"));
        sb.append(String.format("   • Coste Total: %.2f€\n", mercadonaTotal));
        sb.append(String.format("   • ❌ Dinero que pierdes por no ir a Hiperdino cuando toca: %.2f€\n\n",
                Math.max(0, lossIfMercadona)));

        sb.append(String.format("▶️ SI COMPRAS TODO EN HIPERDINO:\n"));
        sb.append(String.format("   • Coste Total: %.2f€\n", hiperdinoTotal));
        sb.append(String.format("   • ❌ Dinero que pierdes por no ir a Mercadona cuando toca: %.2f€\n",
                Math.max(0, lossIfHiperdino)));
        sb.append("====================================================================\n");

        return sb.toString();
    }

    public List<Map<String, Product>> buildShopListData() {
        List<Map<String, Product>> resultList = new ArrayList<>();
        Map<String, Product> allSourcesMap = ProductUtils.BestProductsForAnySources(productInputMap);
        resultList.add(allSourcesMap);
        Map<String, Product> mercadonaMap = filterMapBySource(allSourcesMap, "mercadona");
        resultList.add(mercadonaMap);
        Map<String, Product> hiperdinoMap = filterMapBySource(allSourcesMap, "hiperdino");
        resultList.add(hiperdinoMap);
        return resultList;
    }

    private Map<String, Product> filterMapBySource(Map<String, Product> originalMap, String source) {
        return originalMap.entrySet().stream()
                .filter(entry -> entry.getValue().getSource() != null
                        && entry.getValue().getSource().equalsIgnoreCase(source))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public void processProducts(String input, Product product) {
        productInputMap.computeIfAbsent(input, k -> new ArrayList<>())
                .add(product);
    }
}
