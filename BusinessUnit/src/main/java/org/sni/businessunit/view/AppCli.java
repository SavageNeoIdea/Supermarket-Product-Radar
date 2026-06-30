package org.sni.businessunit.view;
import org.sni.businessunit.model.DefinitiveOptimizedItem;
import org.sni.businessunit.model.OptimizedItem;
import org.sni.businessunit.model.Product;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AppCli {
    private final Function<String, OptimizedItem> searchProvider;
    private final Consumer<DefinitiveOptimizedItem> itemSaver;
    private final Supplier<List<DefinitiveOptimizedItem>> shopListDataProvider;
    private final Runnable listClearer;
    private final ShoppingListCliBuilder shoppingListCliBuilder = new ShoppingListCliBuilder();

    public AppCli(Function<String, OptimizedItem> searchProvider,
                  Consumer<DefinitiveOptimizedItem> itemSaver,
                  Supplier<List<DefinitiveOptimizedItem>> shopListDataProvider,
                  Runnable listClearer) {
        this.searchProvider = searchProvider;
        this.itemSaver = itemSaver;
        this.shopListDataProvider = shopListDataProvider;
        this.listClearer = listClearer;
    }

    public void init() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n🛒 ¡Bienvenido al Optimizador de la Lista de la Compra (Mercadona & Hiperdino)! 🛒" +
                    "\nElige una opción:" +
                    "\n1. Crear lista de la compra" +
                    "\n2. Ver análisis de listas (Conjunta, Exclusivas y 'Tasa de Pereza')" +
                    "\n3. Salir del programa" +
                    "\nResponde seleccionando uno de los números del teclado: ");

            String input = scanner.nextLine().strip();
            if (showOptions(input)) return;
        }
    }

    private boolean showOptions(String input) {
        switch (input) {
            case "1" -> {
                System.out.println("Preparando una nueva lista...");
                listClearer.run();
                initShopList();
            }
            case "2" -> {
                System.out.println("Cargando tu análisis de compra (Conjunta vs Individual)...");
                System.out.println(shoppingListCliBuilder.buildShopList(shopListDataProvider.get()));
            }
            case "3" -> {
                System.out.println("Saliendo. ¡Gracias por usar el Optimizador de Compras!");
                return true;
            }
            default -> System.out.println("❌ Opción no válida, inténtalo de nuevo.");
        }
        return false;
    }

    public void initShopList() {
        System.out.println("===========================================");
        System.out.println("   🛒 ASISTENTE DE LISTA DE COMPRAS 🛒    ");
        System.out.println("===========================================");
        System.out.println("Introduce los productos uno por uno.");
        System.out.println("Escribe 'fin' o pulsa Enter en una línea vacía para terminar.");
        System.out.println("-------------------------------------------");

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ¿Qué producto buscas?: ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("fin") || input.isEmpty()) break;
            OptimizedItem candidates = searchProvider.apply(input);
            if (candidates.mercadonaBestList().isEmpty() && candidates.hiperdinoBestList().isEmpty()) {
                System.out.println("❌ No se encontraron resultados en ningún supermercado para: " + input);
                continue;
            }

            Product chosenMercadona = askUserForProduct(sc, candidates.mercadonaBestList(), "MERCADONA");
            Product chosenHiperdino = askUserForProduct(sc, candidates.hiperdinoBestList(), "HIPERDINO");

            if (chosenMercadona == null && chosenHiperdino == null) {
                System.out.println("⚠️ Producto descartado (no seleccionaste opciones en ningún supermercado).");
                continue;
            }

            Product chosenJoint = getProduct(chosenMercadona, chosenHiperdino);

            DefinitiveOptimizedItem finalItem = new DefinitiveOptimizedItem(
                    input, chosenJoint, chosenMercadona, chosenHiperdino
            );
            itemSaver.accept(finalItem);

            System.out.println("✅ Guardado. (Mejor opción global asignada por rentabilidad: " +
                    (chosenJoint != null ? chosenJoint.getName() + " por " + chosenJoint.getPrice() + "€" : "Ninguna") + ")");
        }
        System.out.println("Lista de la compra completada con " + shopListDataProvider.get().size() + " productos.");
    }

    private static Product getProduct(Product chosenMercadona, Product chosenHiperdino) {
        if (chosenMercadona != null && chosenHiperdino != null) {
            double costPerUnitMercadona = chosenMercadona.getPrice() /
                    (chosenMercadona.getQuantity() * chosenMercadona.getPackageQuantity() * chosenMercadona.getMeasure().getFactorToSI());

            double costPerUnitHiperdino = chosenHiperdino.getPrice() /
                    (chosenHiperdino.getQuantity() * chosenHiperdino.getPackageQuantity() * chosenHiperdino.getMeasure().getFactorToSI());
            Product chosenJoint = (costPerUnitMercadona <= costPerUnitHiperdino) ? chosenMercadona : chosenHiperdino;
        } else if (chosenMercadona != null) {
            Product chosenJoint = chosenMercadona;
        } else {
            Product chosenJoint = chosenHiperdino;
        }
        return null;
    }

    private Product askUserForProduct(Scanner sc, List<Product> options, String storeName) {
        if (options == null || options.isEmpty()) {
            System.out.println("⚠️ No hay opciones disponibles en " + storeName + ".");
            return null;
        }

        System.out.println("\n--- 🛒 Opciones en " + storeName + " ---");
        System.out.printf("%-3s | %-35s | %-12s | %-8s | %-12s%n", "Nº", "Producto", "Formato", "Precio", "Prec. Est.");
        System.out.println("-------------------------------------------------------------------------------");

        for (int i = 0; i < options.size(); i++) {
            Product p = options.get(i);

            String format = p.getPackageQuantity() > 1
                    ? String.format("%duds, %.2f%s", p.getPackageQuantity(), p.getQuantity(), p.getMeasure())
                    : String.format("%.2f%s", p.getQuantity(), p.getMeasure());


            double pricePerStandardUnit = (p.getPrice() / p.getQuantity());

            System.out.printf("%-3d | %-80.80s | %-12s | %-6.2f€ | %.2f€/%s%n",
                    (i + 1), p.getName(), format, p.getPrice(), pricePerStandardUnit, p.getMeasure());
        }

        while (true) {
            System.out.print("\n👉 Selecciona número (1-" + options.size() + ") o [Enter] para omitir: ");
            String userInput = sc.nextLine().trim();

            if (userInput.isEmpty()) return null;

            try {
                int selection = Integer.parseInt(userInput);
                if (selection >= 1 && selection <= options.size()) {
                    return options.get(selection - 1);
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("⚠️ Entrada inválida.");
        }
    }
}