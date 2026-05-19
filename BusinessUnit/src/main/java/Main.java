import controller.Controller;
import controller.feeder.ProductFeeder;
import controller.store.activemq.ConfigReader;
import controller.store.reader.EventReader;
import controller.store.activemq.ActivemqSubscriptor;
import controller.store.sqlite.IAService;
import controller.store.sqlite.SQLiteConnection;
import controller.store.sqlite.SQLiteQuery;
import controller.store.sqlite.SqLiteDatamartStore;

void main() {
    IAService iaService = new IAService();
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