import org.sni.businessunit.controller.Controller;
import org.sni.businessunit.controller.feeder.ProductFeeder;
import org.sni.businessunit.controller.feeder.EmbeddingService;
import org.sni.businessunit.controller.activemq.ActiveMqConfig;
import org.sni.businessunit.controller.activemq.ActiveMqSubscriptor;
import org.sni.businessunit.controller.activemq.ConfigReader;
import org.sni.businessunit.controller.reader.EventReader;
import org.sni.businessunit.store.sqlite.SQLiteConnection;
import org.sni.businessunit.store.sqlite.SQLiteQuery;
import org.sni.businessunit.store.sqlite.SqLiteDatamartStore;

void main() {
    EmbeddingService iaService = new EmbeddingService();

    String topic = new ConfigReader().
            loadConfig("subscribers", "businessUnitSubscriber").get("topicName");
    EventReader eventReader = new EventReader("eventstore", topic);
    ProductFeeder productFeeder = new ProductFeeder(iaService);

    SQLiteConnection sqLiteConnection = new SQLiteConnection();
    SqLiteDatamartStore sqLiteDatamartStore = new SqLiteDatamartStore(sqLiteConnection);

    ActiveMqConfig activeMqConfig = new ActiveMqConfig();
    activeMqConfig.initConfigurations();

    ActiveMqSubscriptor activeMqSuscription = new ActiveMqSubscriptor(activeMqConfig);
    Controller controller = new Controller(eventReader, productFeeder, sqLiteDatamartStore,
            new SQLiteQuery(sqLiteConnection, iaService), activeMqSuscription);
    controller.init();
}