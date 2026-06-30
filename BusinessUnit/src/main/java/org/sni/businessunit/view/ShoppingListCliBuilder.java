package org.sni.businessunit.view;
import org.sni.businessunit.model.DefinitiveOptimizedItem;
import org.sni.businessunit.model.Product;
import java.util.List;

public class ShoppingListCliBuilder {

    private static final String SEPARATOR = "====================================================================";
    private static final String NL = System.lineSeparator();

    public String buildShopList(List<DefinitiveOptimizedItem> optimizedItems) {
        if (optimizedItems == null || optimizedItems.isEmpty()) {
            return "Error: No hay suficientes datos para calcular las listas de la compra.";
        }

        double jointTotal = 0.0;
        double mercadonaTotal = 0.0;
        double hiperdinoTotal = 0.0;

        StringBuilder sbJoint = new StringBuilder();
        StringBuilder sbMercadona = new StringBuilder();
        StringBuilder sbHiperdino = new StringBuilder();

        sbJoint.append(SEPARATOR).append(NL)
                .append("🛒            1. LISTA DE LA COMPRA CONJUNTA OPTIMIZADA (AMBOS)     ").append(NL)
                .append(SEPARATOR).append(NL);

        sbMercadona.append(SEPARATOR).append(NL)
                .append("🛒            2. LISTA DE LA COMPRA EXCLUSIVA EN MERCADONA          ").append(NL)
                .append(SEPARATOR).append(NL);

        sbHiperdino.append(SEPARATOR).append(NL)
                .append("🛒            3. LISTA DE LA COMPRA EXCLUSIVA EN HIPERDINO          ").append(NL)
                .append(SEPARATOR).append(NL);

        for (DefinitiveOptimizedItem item : optimizedItems) {

            Product jointProd = item.selectedJoint();
            if (jointProd != null) {
                appendProductLine(sbJoint, jointProd);
                jointTotal += jointProd.getPrice();
            } else {
                sbJoint.append(String.format("- [NO DISP.] %s: Producto no disponible para optimizar%s", item.userInput(), NL));
            }

            Product mercadonaProd = item.selectedMercadona();
            if (mercadonaProd != null) {
                appendProductLine(sbMercadona, mercadonaProd);
                mercadonaTotal += mercadonaProd.getPrice();
            } else {
                sbMercadona.append(String.format("- [MERCADONA] %s: ❌ No disponible en esta tienda%s", item.userInput(), NL));
            }

            Product hiperdinoProd = item.selectedHiperdino();
            if (hiperdinoProd != null) {
                appendProductLine(sbHiperdino, hiperdinoProd);
                hiperdinoTotal += hiperdinoProd.getPrice();
            } else {
                sbHiperdino.append(String.format("- [HIPERDINO] %s: ❌ No disponible en esta tienda%s", item.userInput(), NL));
            }
        }

        sbJoint.append("--------------------------------------------------------------------").append(NL)
                .append(String.format("💰 COSTE TOTAL OPTIMIZADO (Yendo a ambos): %.2f€%s", jointTotal, NL))
                .append(SEPARATOR).append(NL).append(NL);

        sbMercadona.append("--------------------------------------------------------------------").append(NL)
                .append(String.format("💰 COSTE TOTAL EN MERCADONA: %.2f€%s", mercadonaTotal, NL))
                .append(SEPARATOR).append(NL).append(NL);

        sbHiperdino.append("--------------------------------------------------------------------").append(NL)
                .append(String.format("💰 COSTE TOTAL EN HIPERDINO: %.2f€%s", hiperdinoTotal, NL))
                .append(SEPARATOR).append(NL).append(NL);

        StringBuilder sbAnalysis = new StringBuilder();
        sbAnalysis.append(SEPARATOR).append(NL)
                .append("📊               4. ANÁLISIS DE PÉRDIDAS Y COMPARATIVA              ").append(NL)
                .append(SEPARATOR).append(NL);

        appendAnalysis(sbAnalysis, "MERCADONA", mercadonaTotal, jointTotal);
        appendAnalysis(sbAnalysis, "HIPERDINO", hiperdinoTotal, jointTotal);
        sbAnalysis.append(SEPARATOR).append(NL);

        return String.valueOf(sbJoint) +
                sbMercadona +
                sbHiperdino +
                sbAnalysis;
    }

    private void appendProductLine(StringBuilder sb, Product p) {
        sb.append(String.format("- [%-10s] %-30s: %.2f€ | Cantidad: %.2f (%s)%s",
                p.getSource().toUpperCase(), p.getName(), p.getPrice(), p.getQuantity(), p.getMeasure(), NL));
    }

    private void appendAnalysis(StringBuilder sb, String store, double storeTotal, double jointTotal) {
        double loss = Math.max(0, storeTotal - jointTotal);
        sb.append(String.format("▶️ SI COMPRAS TODO EN %s:%s", store, NL))
                .append(String.format("   • Coste Total: %.2f€%s", storeTotal, NL))
                .append(String.format("   • ❌ Dinero que pierdes por comodidad: %.2f€%s%s", loss, NL, NL));
    }
}