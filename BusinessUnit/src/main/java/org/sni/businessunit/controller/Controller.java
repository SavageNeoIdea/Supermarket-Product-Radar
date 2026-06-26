package org.sni.businessunit.controller;
import org.sni.businessunit.controller.feeder.Feeder;
import org.sni.businessunit.model.Product;
import org.sni.businessunit.store.DatamartStore;
import org.sni.businessunit.store.SearchQuery;
import org.sni.businessunit.controller.activemq.Subscriptor;
import org.sni.businessunit.controller.reader.DataReader;
import org.sni.businessunit.view.AppManager;

import java.util.List;
import java.util.Map;

public class Controller {
    private final DataReader eventReader;
    private final Feeder feeder;
    private final DatamartStore store;
    private final AppManager shoppingListManager;
    private final Subscriptor subscriptor;

    public Controller(DataReader eventReader, Feeder feeder, DatamartStore store, SearchQuery searchQuery, Subscriptor subscriptor) {
        this.eventReader = eventReader;
        this.feeder = feeder;
        this.store = store;
        this.shoppingListManager = new AppManager(feeder, searchQuery);
        this.subscriptor = subscriptor;
    }

    public void init() {
        synchronizePastDayData();
        subscriptor.start(this::processLiveEvent);
        try {
            shoppingListManager.runInteractiveLoop();
        } finally {
            subscriptor.close();
            System.out.println("Aplicación cerrada de forma segura.");
        }
    }

    private void synchronizePastDayData() {
        Map<String, List<String>> rawEvents = eventReader.readLastDay();
        if (!rawEvents.isEmpty()) {
            List<Product> products = feeder.processData(rawEvents);
            store.storeAllData(products);
        }
    }

    private void processLiveEvent(String rawEventString) {
        try {
            String source = feeder.extractSourceFromJson(rawEventString);
            if (source == null) {
                System.out.println("WARN: Evento sin 'ss' ignorado.");
                return;
            }
            Product product = feeder.processData(source, rawEventString);
            if (product != null) {
                store.storeAllData(List.of(product));
            }
        } catch (Exception e) {
            System.err.println("Error procesando evento en vivo: " + e.getMessage());
        }
    }
}