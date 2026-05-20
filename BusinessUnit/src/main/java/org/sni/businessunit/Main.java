import org.sni.businessunit.controller.Controller;
import org.sni.businessunit.controller.feeder.ProductFeeder;
import org.sni.businessunit.store.EmbeddingService;
import org.sni.businessunit.store.activemq.ActivemqSubscriptor;
import org.sni.businessunit.store.activemq.ConfigReader;
import org.sni.businessunit.store.reader.EventReader;
import org.sni.businessunit.store.sqlite.SQLiteConnection;
import org.sni.businessunit.store.sqlite.SQLiteDatabaseInitializer;
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
    ActivemqSubscriptor activeMQSuscription = new ActivemqSubscriptor(productFeeder, sqLiteDatamartStore);
    Controller controller = new Controller(eventReader, productFeeder, sqLiteDatamartStore,
            new SQLiteQuery(sqLiteConnection, iaService), activeMQSuscription);
    controller.init();
}