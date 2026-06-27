import org.sni.businessunit.controller.Controller;
import org.sni.businessunit.controller.activemq.Subscriptor;
import org.sni.businessunit.controller.embedding.SemanticEngine;
import org.sni.businessunit.controller.feeder.ProductFeeder;
import org.sni.businessunit.controller.embedding.EmbeddingService;
import org.sni.businessunit.controller.activemq.ActiveMqConfig;
import org.sni.businessunit.controller.activemq.ActiveMqSubscriptor;
import org.sni.businessunit.controller.activemq.ConfigReader;
import org.sni.businessunit.controller.reader.EventReader;
import org.sni.businessunit.controller.shoppinglist.ProductCharger;
import org.sni.businessunit.controller.shoppinglist.SearchQuery;
import org.sni.businessunit.controller.shoppinglist.ShopListBuilder;
import org.sni.businessunit.store.DatamartStore;
import org.sni.businessunit.store.sqlite.SQLiteConnection;
import org.sni.businessunit.controller.shoppinglist.SQLiteQuery;
import org.sni.businessunit.store.sqlite.SqLiteDatamartStore;

public static void main(String[] args) {
    final SemanticEngine iaService = new EmbeddingService();
    final SQLiteConnection sqliteConn = new SQLiteConnection();
    final EventReader eventReader = new EventReader("eventstore", getConfigTopic());
    final ProductFeeder productFeeder = new ProductFeeder(iaService);
    final DatamartStore datamartStore = new SqLiteDatamartStore(sqliteConn);
    final Subscriptor activeMqSub = new ActiveMqSubscriptor(getInitializedMqConfig());
    final ProductCharger charger = new ProductCharger(sqliteConn);
    final SearchQuery searchQuery = new SQLiteQuery(charger, iaService);
    final ShopListBuilder shopListBuilder = new ShopListBuilder(searchQuery);

    Controller controller = new Controller(
            eventReader,
            productFeeder,
            datamartStore,
            activeMqSub,
            shopListBuilder
    );
    controller.init();
}

private static String getConfigTopic() {
    return new ConfigReader().loadConfig("subscribers", "businessUnitSubscriber").get("topicName");
}

private static ActiveMqConfig getInitializedMqConfig() {
    ActiveMqConfig config = new ActiveMqConfig();
    config.initConfigurations();
    return config;
}