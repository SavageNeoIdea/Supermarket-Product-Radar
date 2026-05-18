import Controller.Controller;
import store.EventStore;
import store.activemqSubscriptor;

public class Main {
    public static void main(String[] args) {
        EventStore eventStore = new EventStore();
        Controller controller = new Controller(new activemqSubscriptor(eventStore));
        controller.init();
    }
}