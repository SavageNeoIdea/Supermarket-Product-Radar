package controller.shoppingListApp;
import controller.feeder.Feeder;
import controller.store.sqlite.SQLiteQuery;
import model.Product;
import java.util.*;
import java.util.function.Consumer;

public class AppManager {

    private final Feeder feeder;
    public List<String> customerInputs = new ArrayList<>();
    public ShoppingListBuilder builder = new ShoppingListBuilder();

    public AppManager(Feeder feeder) {
        this.feeder = feeder;
    }

    public void initApp() {
        Consumer<String> cliListener = this::processInput;
        AppCli cli = new AppCli(cliListener, builder);
        cli.init();
    }

    private void processInput(String input) {
        customerInputs.add(input);
        Map<String, Map<String, List<String>>> data = SQLiteQuery.searchQuery(input);
        Map<String, List<String>> sourceEventMap = data.get(input);
        if (sourceEventMap != null) {
            for (Map.Entry<String, List<String>> entry : sourceEventMap.entrySet()) {
                String source = entry.getKey();
                for (String event : entry.getValue()) {
                    Product product = feeder.processData(source, event);
                    builder.processProducts(input, product);
                }
            }
        }
    }
}