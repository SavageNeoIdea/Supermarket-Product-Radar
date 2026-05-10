import controller.Controller;
import controller.feeder.ProductFeeder;
import controller.reader.EventReader;

void main() {
    EventReader eventReader = new EventReader("D:\\Documentos\\DACD\\Supermarket-Product-Radar\\eventstore", "product");
    ProductFeeder productFeeder = new ProductFeeder();
    Controller controller = new Controller(eventReader, productFeeder);
    controller.init();
}



