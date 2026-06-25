import controller.Controller;
import model.Publisher;
import store.ActivemqEventConsumer;
import store.ConfigReader;
import store.EventStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        List<Publisher> publisherList = new ArrayList<>();
        publisherList.add(new Publisher("product", "mercadona"));
        publisherList.add(new Publisher("product", "hiperdino"));
        EventStore eventStore = new EventStore(publisherList);
        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("subscribers", "eventStoreSubscriber");
        String brokerUrl = config.get("brokerUrl");
        String topicName = config.get("topicName");
        String clientId = config.get("clientId");
        String subscriptionName = config.get("subscriptionName");
        Controller controller = new Controller(eventStore, new ActivemqEventConsumer(eventStore, brokerUrl, topicName, clientId, subscriptionName));
        controller.init();
    }
}