import controller.Controller;
import controller.feeder.ProductFeeder;
import controller.reader.EventReader;
import controller.store.sqlite.SQLiteConnection;
import controller.store.sqlite.SQLiteQuery;
import controller.store.sqlite.SqLiteDatamartStore;

void main() {
    EventReader eventReader = new EventReader("D:\\Documentos\\DACD\\Supermarket-Product-Radar\\eventstore", "product");
    ProductFeeder productFeeder = new ProductFeeder();
    SQLiteConnection sqLiteConnection = new SQLiteConnection();
    SqLiteDatamartStore sqLiteDatamartStore = new SqLiteDatamartStore(sqLiteConnection);
    Controller controller = new Controller(eventReader, productFeeder, sqLiteDatamartStore, new SQLiteQuery(sqLiteConnection));
    controller.init();
}



