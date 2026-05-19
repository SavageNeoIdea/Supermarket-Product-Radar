import controller.Controller;
import controller.feeder.ProductFeeder;
import store.activemq.ConfigReader;
import store.reader.EventReader;
import store.activemq.ActivemqSubscriptor;
import store.EmbeddingService;
import store.sqlite.SQLiteConnection;
import store.sqlite.SQLiteQuery;
import store.sqlite.SqLiteDatamartStore;

void main() {
    EmbeddingService iaService = new EmbeddingService();
    String topic = new ConfigReader().
            loadConfig("subscribers","businessUnitSubscriber").get("topicName");
    EventReader eventReader = new EventReader("eventstore", topic);
    ProductFeeder productFeeder = new ProductFeeder();
    SQLiteConnection sqLiteConnection = new SQLiteConnection();
    SqLiteDatamartStore sqLiteDatamartStore = new SqLiteDatamartStore(sqLiteConnection, iaService);
    ActivemqSubscriptor activeMQSuscription = new ActivemqSubscriptor(productFeeder, sqLiteDatamartStore);
    Controller controller = new Controller(eventReader, productFeeder, sqLiteDatamartStore,
            new SQLiteQuery(sqLiteConnection, iaService), activeMQSuscription);
    controller.init();
}