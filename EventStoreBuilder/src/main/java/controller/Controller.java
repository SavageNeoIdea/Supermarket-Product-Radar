package controller;
import store.ActivemqEventConsumer;
import store.BrokerConsumer;
import store.EventStore;

public class Controller {
    private final EventStore eventStore;
    private final BrokerConsumer consumer;
    public Controller(EventStore eventStore, ActivemqEventConsumer consumer) {
        this.eventStore = eventStore;
        this.consumer = consumer;
    }
    public void init() {
        eventStore.prepareEventStorage();
        consumer.startBrokerConsumer();
    }

}
