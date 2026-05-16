package controller.shoppingListCreator;

import java.util.Scanner;
import java.util.function.Consumer;

public class ShoppingListCli {
    private final Consumer<String> inputStream;
    public ShoppingListCli(Consumer<String> inputStream) {
        this.inputStream = inputStream;
    }
    public void init(){
        while (true) {
            System.out.println("Bienvenido a la lista de compra automatica de Hiperdino!: elige una opción:" +
                    "\n1. Crear lista de la compra" +
                    "\n2. Consultar una lista" +
                    "\n3. Salir del programa:" +
                    "\nResponde seleccionando uno de los números del teclado: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().strip();

            switch (input) {
                case "1" -> {
                    System.out.println("Creando lista...");
                    initShopList();
                }
                case "2" -> {
                    System.out.println("Consultando lista...");
                }
                case "3" -> System.out.println("Saliendo. ¡Gracias por comprar en Hiperdino!");

                default -> System.out.println("Opción no válida, inténtalo de nuevo.");
            }

            break;
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
    }
}
