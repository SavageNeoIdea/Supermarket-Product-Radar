package org.sni.spr.hiperdino;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductIdentifierTest {

    @Test
    void testIdentifyAttrsSingleItem() {

        String productText = "Leche entera Pascual 1 l";
        String priceText = "1,25 €";

        ProductIdentifier identifier = new ProductIdentifier(productText, priceText);

        assertEquals("Leche entera Pascual", identifier.getName());
        assertEquals(1, identifier.getQty());
        assertEquals(UnitsOfMeasurement.l, identifier.getMeasure());
        assertEquals(1.25, identifier.getPrice(), 0.001);
    }

    @Test
    void testIdentifyAttrsWithPackage() {

        String productText = "Zumo de naranja 6x250 ml";
        String priceText = "3,50";

        ProductIdentifier identifier = new ProductIdentifier(productText, priceText);

        assertEquals("Zumo de naranja", identifier.getName());
        assertEquals(6, identifier.getPackageQty());
        assertEquals(1500, identifier.getQty(), "La cantidad total debe ser packageQty * qty (6 * 250)");
        assertEquals(UnitsOfMeasurement.ml, identifier.getMeasure());
        assertEquals(3.50, identifier.getPrice(), 0.001);
    }

    @Test
    void testIdentifyAttrsGramMeasurement() {

        String productText = "Queso tierno rallado 250 g";
        String priceText = "2,15";

        ProductIdentifier identifier = new ProductIdentifier(productText, priceText);

        assertEquals("Queso tierno rallado", identifier.getName());
        assertEquals(250, identifier.getQty());
        assertEquals(UnitsOfMeasurement.g, identifier.getMeasure());
        assertEquals(2.15, identifier.getPrice(), 0.001);
    }
}