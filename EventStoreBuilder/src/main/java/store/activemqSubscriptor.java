package store;
import org.apache.activemq.ActiveMQConnectionFactory;
import Controller.ConfigReader;
import javax.jms.*;
import java.util.Map;

public class activemqSubscriptor implements MessageListener, Subscriptor {

    private Connection connection;
    private Session session;
    private EventStore eventStore;

    public activemqSubscriptor(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void start() {
        try {
            ConfigReader reader = new ConfigReader();
            Map<String, String> config = reader.loadConfig("subscribers","eventStoreSubscriber");
            if (config == null) {
                System.err.println("ERROR: No se pudo cargar la configuración de eventStoreBuilder.");
                return;
            }
            String brokerUrl = config.get("brokerUrl");
            String topicName = config.get("topicName");
            String clientId = config.get("clientId");
            String subscriptionName = config.get("subscriptionName");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();
            connection.setClientID(clientId);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
            consumer.setMessageListener(this);
            connection.start();
            System.out.println("EventStoreBuilder: Suscrito al topic '" + topicName + "'. Esperando eventos...");
        } catch (JMSException e) {
            System.err.println("Error en el Suscriptor: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String json = ((TextMessage) message).getText();
                eventStore.saveEvent("Product", json);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}