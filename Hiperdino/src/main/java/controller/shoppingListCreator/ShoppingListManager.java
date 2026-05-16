package controller.shoppingListCreator;

import controller.store.sqlite.SQLiteQuery;

import java.util.*;

public class ShoppingListManager {

    public Map<String, String> products = new LinkedHashMap<>();
    public final Map<String, Map<String, String>> alternatives = new HashMap<>();
    public List<String> initialShopList = new ArrayList<>();
    public List<Map<String, List<String>>> searchShopList = new ArrayList<>();

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
            initialShopList.add(input);
        }

        System.out.println("\n✅ Lista finalizada. Tienes " + initialShopList.size() + " productos en tu lista.");
        System.out.println("Tus productos: " + initialShopList);
        shopingListSearch();
    }

    public void shopingListSearch(){
        for (String input : initialShopList){
            searchShopList.add(SQLiteQuery.searchQuery(input));
        }

        for (Map<String, List<String>> inputData :  searchShopList){
            for (Map.Entry<String, List<String>> data: inputData.entrySet()){
                System.out.println(STR."Resultados de busqueda para el input: \{data.getKey()}");
                for (String product :  data.getValue()){
                    System.out.println(product);
                }
            }
        }

    }

    public void saveShopingList(){

    }
}
