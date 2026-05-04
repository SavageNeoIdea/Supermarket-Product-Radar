package org.sni.spr.hiperdino.controller.simulationForTesting;

import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;
import java.util.ArrayList;
import java.util.List;

public class HiperdinoSimulation {

    public List<HiperdinoProduct> init() {
        List<HiperdinoProduct> products = new ArrayList<>();

        products.add(new HiperdinoProduct("SKU-001", "8410001", "HiperDino", "Lácteos", "Leche", "Leche Entera 1L", 1, 1, UnitsOfMeasurement.l, 0.92, false, "img01.jpg"));
        products.add(new HiperdinoProduct("SKU-002", "8410002", "Danone", "Lácteos", "Yogures", "Yogur Natural Pack 4", 125, 4, UnitsOfMeasurement.g, 1.85, false, "img02.jpg"));
        products.add(new HiperdinoProduct("SKU-003", "8410003", "Pascual", "Lácteos", "Mantequilla", "Mantequilla con Sal", 250, 1, UnitsOfMeasurement.g, 2.10, false, "img03.jpg"));

        products.add(new HiperdinoProduct("SKU-004", "8410004", "Gallo", "Pasta", "Macarrones", "Macarrones Plumas", 500, 1, UnitsOfMeasurement.g, 1.20, false, "img04.jpg"));
        products.add(new HiperdinoProduct("SKU-005", "8410005", "Schar", "Pasta", "Sin Gluten", "Espagueti Sin Gluten", 400, 1, UnitsOfMeasurement.g, 3.45, true, "img05.jpg"));
        products.add(new HiperdinoProduct("SKU-006", "8410006", "Brillante", "Arroz", "Básico", "Arroz Redondo 1kg", 1, 1, UnitsOfMeasurement.kg, 1.55, false, "img06.jpg"));
        products.add(new HiperdinoProduct("SKU-007", "8410007", "Cuétara", "Galletas", "Desayuno", "Galletas María Oro", 800, 1, UnitsOfMeasurement.g, 3.10, false, "img07.jpg"));

        products.add(new HiperdinoProduct("SKU-008", "8410008", "Coca-Cola", "Bebidas", "Refrescos", "Coca-Cola Zero 1.5L", 150, 1, UnitsOfMeasurement.cl, 1.95, false, "img08.jpg"));
        products.add(new HiperdinoProduct("SKU-009", "8410009", "Bezoya", "Bebidas", "Agua", "Agua Mineral 1.5L", 1500, 1, UnitsOfMeasurement.ml, 0.65, false, "img09.jpg"));
        products.add(new HiperdinoProduct("SKU-010", "8410010", "Tropical", "Bebidas", "Cerveza", "Cerveza Lata 33cl", 33, 1, UnitsOfMeasurement.cl, 0.70, false, "img10.jpg"));
        products.add(new HiperdinoProduct("SKU-011", "8410011", "Don Simón", "Bebidas", "Zumos", "Zumo de Naranja 1L", 1, 1, UnitsOfMeasurement.l, 1.40, false, "img11.jpg"));

        products.add(new HiperdinoProduct("SKU-012", "8410012", "HiperDino", "Fruta", "Granel", "Plátano de Canarias", 1, 1, UnitsOfMeasurement.kg, 2.20, false, "img12.jpg"));
        products.add(new HiperdinoProduct("SKU-013", "8410013", "HiperDino", "Verdura", "Malla", "Papas Selección 3kg", 3, 1, UnitsOfMeasurement.kg, 4.50, false, "img13.jpg"));
        products.add(new HiperdinoProduct("SKU-014", "8410014", "El Pozo", "Carnicería", "Embutido", "Jamón Cocido Extra", 200, 1, UnitsOfMeasurement.g, 2.80, false, "img14.jpg"));

        products.add(new HiperdinoProduct("SKU-015", "8410015", "Ariel", "Limpieza", "Ropa", "Detergente Cápsulas 20uds", 20, 1, UnitsOfMeasurement.uds, 8.95, false, "img15.jpg"));
        products.add(new HiperdinoProduct("SKU-016", "8410016", "Fairy", "Limpieza", "Vajilla", "Lavavajillas Mano", 650, 1, UnitsOfMeasurement.ml, 3.25, false, "img16.jpg"));
        products.add(new HiperdinoProduct("SKU-017", "8410017", "Colgate", "Higiene", "Dental", "Pasta de Dientes", 75, 1, UnitsOfMeasurement.ml, 1.99, false, "img17.jpg"));
        products.add(new HiperdinoProduct("SKU-018", "8410018", "Dodot", "Bebé", "Pañales", "Pañales Talla 4", 44, 1, UnitsOfMeasurement.ud, 14.50, false, "img18.jpg"));

        products.add(new HiperdinoProduct("SKU-019", "8410019", "Pescanova", "Congelados", "Pescado", "Varitas de Merluza", 400, 1, UnitsOfMeasurement.g, 3.75, false, "img19.jpg"));
        products.add(new HiperdinoProduct("SKU-020", "8410020", "Dr. Oetker", "Congelados", "Pizza", "Pizza Ristorante", 350, 1, UnitsOfMeasurement.g, 4.15, false, "img20.jpg"));

        return products;
    }
}