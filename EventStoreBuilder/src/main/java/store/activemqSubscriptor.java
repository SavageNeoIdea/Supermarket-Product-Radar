package store;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.Map;

public class activemqSubscriptor implements MessageListener, Subscriptor {

    private final String brokerUrl;
    private final String topicName;
    private final String clientId;
    private final String subscriptionName;
    private Connection connection;
    private Session session;
    private final EventStore eventStore;

    public activemqSubscriptor(EventStore eventStore) {
        this.eventStore = eventStore;
        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("subscribers", "eventStoreSubscriber");
        if (config == null) {
            throw new RuntimeException("ERROR: Could not load configuration for eventStoreSubscriber");
        }

        this.brokerUrl = config.get("brokerUrl");
        this.topicName = config.get("topicName");
        this.clientId = config.get("clientId");
        this.subscriptionName = config.get("subscriptionName");
    }

    @Override
    public void start() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();
            connection.setClientID(clientId);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
            consumer.setMessageListener(this);
            connection.start();
            System.out.println("EventStoreBuilder: Subscribed to topic '" + topicName + "'");

        } catch (JMSException e) {
            System.err.println("Error starting subscriber: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String json = textMessage.getText();
                eventStore.saveEvent("Product", json);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}