import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class Suscriptor implements MessageListener {

    private final String BROKER_URL = "tcp://localhost:61616";
    private final String TOPIC_NAME = "Products";
    private final String CLIENT_ID = "EventStoreBuilder_Subscriber";
    private final String SUBSCRIPTION_NAME = "MainEventStoreSub";

    private Connection connection;
    private Session session;
    private EventStore eventStore; // Referencia a tu otra clase

    public Suscriptor(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void start() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            factory.setTrustAllPackages(true);

            connection = factory.createConnection();
            connection.setClientID(CLIENT_ID);

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);

            MessageConsumer consumer = session.createDurableSubscriber(topic, SUBSCRIPTION_NAME);
            consumer.setMessageListener(this);

            connection.start();
            System.out.println("EventStoreBuilder: Suscrito al topic 'Products'. Esperando eventos...");

        } catch (JMSException e) {
            System.err.println("Error en el Suscriptor: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String json = ((TextMessage) message).getText();
                eventStore.saveEvent(json);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}