package org.sni.businessunit.view;

import org.sni.businessunit.controller.feeder.Feeder;
import org.sni.businessunit.model.Product;
import org.sni.businessunit.store.SearchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AppManager {

    private final Feeder feeder;
    private final SearchQuery searchQuery;
    public List<String> customerInputs = new ArrayList<>();
    public ShoppingListBuilder builder;

    public AppManager(Feeder feeder, SearchQuery searchQuery) {
        this.feeder = feeder;
        this.searchQuery = searchQuery;
        this.builder = new ShoppingListBuilder();
    }

    public void runInteractiveLoop() {
        Consumer<String> cliListener = this::processInput;
        Supplier<String> cliShopListProvider = this::ShopListInput;
        AppCli cli = new AppCli(cliListener, cliShopListProvider, builder);
        cli.init();
    }

    private String ShopListInput() {
        return builder.buildShopList();
    }

    private void processInput(String input) {
        customerInputs.add(input);
        Map<String, Map<String, List<String>>> data = searchQuery.searchQuery(input);
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