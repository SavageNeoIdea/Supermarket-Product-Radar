package org.sni.businessunit.view;

import org.sni.businessunit.model.OptimizedItem;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AppCli {
    private final Consumer<String> inputStream;
    private final Supplier<List<OptimizedItem>> shopListDataProvider;
    private final ShoppingListCliBuilder shoppingListCliBuilder = new ShoppingListCliBuilder();
    public AppCli(Consumer<String> inputStream, Supplier<List<OptimizedItem>> shopListDataProvider) {
        this.inputStream = inputStream;
        this.shopListDataProvider = shopListDataProvider;
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

            switch (input) {
                case "1" -> {
                    System.out.println("Calculando y optimizando listas de la compra...");
                    initShopList();
                }
                case "2" -> {
                    System.out.println("Cargando tu análisis de compra (Conjunta vs Individual)...");
                    System.out.println(shoppingListCliBuilder.buildShopList(shopListDataProvider.get()));
                }
                case "3" -> {
                    System.out.println("Saliendo. ¡Gracias por usar el Optimizador de Compras!");
                    return;
                }
                default -> System.out.println("❌ Opción no válida, inténtalo de nuevo.");
            }
        }
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
            System.out.print("> ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("fin") || input.isEmpty()) break;
            inputStream.accept(input);
        }
        System.out.println("Lista de la compra guardada y procesada.");
    }
}