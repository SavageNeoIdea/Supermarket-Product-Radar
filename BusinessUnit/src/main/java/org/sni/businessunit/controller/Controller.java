package org.sni.businessunit.controller;
import org.sni.businessunit.controller.feeder.Feeder;
import org.sni.businessunit.controller.shoppinglist.ShopListBuilder;
import org.sni.businessunit.model.Product;
import org.sni.businessunit.controller.store.DatamartStore;
import org.sni.businessunit.controller.activemq.Subscriptor;
import org.sni.businessunit.controller.reader.DataReader;
import org.sni.businessunit.view.AppCli;
import java.util.List;
import java.util.Map;

public class Controller {
    private final DataReader eventReader;
    private final Feeder feeder;
    private final DatamartStore store;
    private final Subscriptor subscriptor;
    private final ShopListBuilder shopListBuilder;

    public Controller(DataReader eventReader, Feeder feeder, DatamartStore store, Subscriptor subscriptor, ShopListBuilder shopListBuilder) {
        this.eventReader = eventReader;
        this.feeder = feeder;
        this.store = store;
        this.subscriptor = subscriptor;
        this.shopListBuilder = shopListBuilder;
    }

    public void init() {
        synchronizePastDayData();
        subscriptor.start(this::processLiveEvent);
        try {
            AppCli appCli = new AppCli(
                    shopListBuilder::findCandidates,
                    shopListBuilder::saveDefinitiveItem,
                    shopListBuilder::getShoppingList,
                    shopListBuilder::clearList
            );
            appCli.init();
        } finally {
            subscriptor.close();
            System.out.println("Aplicación cerrada de forma segura.");
        }
    }

    private void synchronizePastDayData() {
        Map<String, List<String>> rawEventsPerSource = eventReader.readLastDay();
        if (!rawEventsPerSource.isEmpty()) {
            List<Product> products = feeder.processData(rawEventsPerSource);
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
            if (product != null)
                store.storeAllData(List.of(product));

        } catch (Exception e) {
            System.err.println("Error procesando evento en vivo: " + e.getMessage());
        }
    }
}