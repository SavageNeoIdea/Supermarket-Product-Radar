package controller.shoppingListCreator;
import controller.feeder.Feeder;
import controller.reader.DataReader;
import controller.store.sqlite.SQLiteQuery;
import model.Product;
import java.util.*;
import java.util.function.Consumer;

public class ShoppingListManager {

    private final Feeder feeder;
    public final Map<String, Map<String, String>> alternatives = new HashMap<>();
    public List<String> customerInputs = new ArrayList<>();
    public Map<String, List<Product>> productMap = new LinkedHashMap<>();

    public ShoppingListManager(Feeder feeder) {
        this.feeder = feeder;
    }

    public void initApp() {
        Consumer<String> cliListener = this::processInput;
        ShoppingListCli cli = new ShoppingListCli(cliListener);
        cli.init();
    }

    private void processInput(String input) {
        customerInputs.add(input);
        Map<String, Map<String, List<String>>> data = SQLiteQuery.searchQuery(input);
        Map<String, List<String>> sourceEventMap = data.get(input);
        List<Product> productList = new ArrayList<>();

        if (sourceEventMap != null) {
            for (Map.Entry<String, List<String>> entry : sourceEventMap.entrySet()) {
                for (String event : entry.getValue()){
                    Product product = feeder.processData(entry.getKey(), event);
                    productList.add(product);
                }
            }
            productMap.put(input, productList);
            for (Product product : productList){
                System.out.print(product.getName() + ", ");
            }
        }
    }
}