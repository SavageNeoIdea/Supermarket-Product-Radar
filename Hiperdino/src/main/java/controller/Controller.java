package controller;
import controller.shoppingListApp.AppManager;
import controller.store.DatamartConnection;
import controller.store.DatamartStore;
import controller.store.SearchQuery;
import controller.store.sqlite.SqLiteDatamartStore;
import model.Product;
import controller.reader.DataReader;
import controller.feeder.Feeder;

import java.util.List;
import java.util.Map;

public class Controller {
    private final DataReader eventReader;
    private final Feeder feeder;
    private final DatamartStore store;
    private final AppManager shoppingListManager;

    public Controller(DataReader eventReader, Feeder feeder, DatamartStore store, SearchQuery searchQuery) {
        this.eventReader = eventReader;
        this.feeder = feeder;
        this.store = store;
        this.shoppingListManager = new AppManager(feeder, searchQuery);
    }

    public void init() {
        Map<String, List<String>> rawProducts = eventReader.readLastDay();
        List<Product> products = feeder.processData(rawProducts);;
        store.storeAllData(products);
        shoppingListManager.initApp();
    }
}