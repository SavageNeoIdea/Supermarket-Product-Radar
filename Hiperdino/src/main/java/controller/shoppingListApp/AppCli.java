package controller.shoppingListApp;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AppCli {
    private final Consumer<String> inputStream;
    private final ShoppingListBuilder shoppingListBuilder;

    public AppCli(Consumer<String> inputStream, ShoppingListBuilder shoppingListBuilder) {
        this.inputStream = inputStream;
        this.shoppingListBuilder = shoppingListBuilder;
    }

    public void init(){
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nBienvenido a la lista de compra automatica de Hiperdino!: elige una opción:" +
                    "\n1. Crear lista de la compra" +
                    "\n2. Consultar una lista" +
                    "\n3. Observar la lista creada actual" +
                    "\n4. Salir del programa:" +
                    "\nResponde seleccionando uno de los números del teclado: ");

            String input = scanner.nextLine().strip();

            switch (input) {
                case "1" -> {
                    System.out.println("Creando lista...");
                    initShopList();
                }
                case "2" -> {
                    System.out.println("Consultando lista...");
                }
                case "3" -> {
                    System.out.println("Cargando tu lista actual...");
                    System.out.println(shoppingListBuilder.saveMoneyShopList());
                }
                case "4" -> {
                    System.out.println("Saliendo. ¡Gracias por comprar en Hiperdino!");
                    return;
                }
                default -> System.out.println("Opción no válida, inténtalo de nuevo.");
            }
        }
    }

    public void initShopList(){
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