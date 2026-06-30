import org.sni.businessunit.controller.activemq.ActiveMqConfig;
import  org.sni.businessunit.controller.Controller;
import org.sni.businessunit.controller.feeder.ProductFeeder;
import org.sni.businessunit.controller.embedding.EmbeddingService;
import org.sni.businessunit.controller.activemq.ActiveMqSubscriptor;
import org.sni.businessunit.controller.activemq.ConfigReader;
import org.sni.businessunit.controller.reader.EventReader;
import org.sni.businessunit.controller.shoppinglist.ShopListBuilderDefault;
import org.sni.businessunit.store.sqlite.SQLiteConnection;
import org.sni.businessunit.controller.shoppinglist.SQLiteQuery;
import org.sni.businessunit.store.sqlite.SqLiteDatamartStore;
import java.util.Map;

public static void main(String[] args) {
    final EmbeddingService iaService = new EmbeddingService();
    final SQLiteConnection sqliteConn = new SQLiteConnection();
    final EventReader eventReader = new EventReader("eventstore", getConfigTopic());
    final ProductFeeder productFeeder = new ProductFeeder(iaService);
    final SqLiteDatamartStore datamartStore = new SqLiteDatamartStore(sqliteConn);
    final ActiveMqSubscriptor activeMqSub = new ActiveMqSubscriptor(getActiveMqConfig());
    final SQLiteQuery searchQuery = new SQLiteQuery(sqliteConn, iaService);
    final ShopListBuilderDefault shopListBuilder = new ShopListBuilderDefault(searchQuery);

    Controller controller = new Controller(
            eventReader,
            productFeeder,
            datamartStore,
            activeMqSub,
            shopListBuilder
    );
    controller.init();
}

private static ActiveMqConfig getActiveMqConfig() {
    ConfigReader configReader = new ConfigReader();
    Map<String, String> configMap = configReader.loadConfig("subscribers", "businessUnitSubscriber");
    if (configMap == null) {
        throw new RuntimeException("ERROR: Could not load configuration for businessUnitSubscriber");
    }
    return new ActiveMqConfig(
            configMap.get("brokerUrl"),
            configMap.get("topicName"),
            configMap.get("clientId"),
            configMap.get("subscriptionName"),
            configMap.get("username"),
            configMap.get("password")
    );
}

private static String getConfigTopic() {
    return new ConfigReader().loadConfig("subscribers", "businessUnitSubscriber").get("topicName");
}