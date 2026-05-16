package controller;
import controller.shoppingListCreator.ShoppingListManager;
import controller.store.sqlite.SqLiteDatamartStore;
import model.Product;
import controller.reader.DataReader;
import controller.feeder.Feeder;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Controller {
    private final DataReader eventReader;
    private final Feeder feeder;
    private final ShoppingListManager shoppingListManager = new ShoppingListManager();
    public Controller(DataReader eventReader, Feeder feeder){
        this.eventReader = eventReader;
        this.feeder = feeder;
    }

    public void init(){
        Map<String, List<String>> rawProducts = eventReader.readLastDay();
        List<Product> products = feeder.processData(rawProducts);
        SqLiteDatamartStore store = new SqLiteDatamartStore();
        store.storeAllData(products);
        pmv();
    }

    public void pmv(){
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
                    shoppingListManager.initShopList();
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
}
