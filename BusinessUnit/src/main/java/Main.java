import controller.Controller;
import controller.feeder.ProductFeeder;
import controller.store.reader.EventReader;
import controller.store.activemq.ActivemqSubscriptor;
import controller.store.sqlite.SQLiteConnection;
import controller.store.sqlite.SQLiteQuery;
import controller.store.sqlite.SqLiteDatamartStore;

void main() {
    EventReader eventReader = new EventReader("D:\\Documentos\\DACD\\sprsnp\\eventstore", "product");
    ProductFeeder productFeeder = new ProductFeeder();
    SQLiteConnection sqLiteConnection = new SQLiteConnection();
    SqLiteDatamartStore sqLiteDatamartStore = new SqLiteDatamartStore(sqLiteConnection);
    ActivemqSubscriptor activeMQSuscription = new ActivemqSubscriptor(productFeeder, sqLiteDatamartStore);
    Controller controller = new Controller(eventReader, productFeeder, sqLiteDatamartStore,
            new SQLiteQuery(sqLiteConnection), activeMQSuscription);
    controller.init();
}