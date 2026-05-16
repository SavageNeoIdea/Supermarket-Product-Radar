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
    private final ShoppingListManager shoppingListManager;

    public Controller(DataReader eventReader, Feeder feeder) {
        this.eventReader = eventReader;
        this.feeder = feeder;
        this.shoppingListManager = new ShoppingListManager(feeder);
    }

    public void init() {
        Map<String, List<String>> rawProducts = eventReader.readLastDay();
        List<Product> products = feeder.processData(rawProducts);
        SqLiteDatamartStore store = new SqLiteDatamartStore();
        store.storeAllData(products);
        shoppingListManager.initApp();
    }
}