package store;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActivemqEventConsumer implements MessageListener, BrokerConsumer {

    private final String brokerUrl;
    private final String topicName;
    private final String clientId;
    private final String subscriptionName;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private final EventStore eventStore;

    public ActivemqEventConsumer(EventStore eventStore, String brokerUrl, String topicName, String clientId, String subscriptionName) {
        this.eventStore = eventStore;
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientId = clientId;
        this.subscriptionName = subscriptionName;
    }

    @Override
    public void startBrokerConsumer() {
        try {
            initializeActiveMQ();
            System.out.println("EventStoreBuilder: Subscribed to topic '" + topicName + "'");
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeActiveMQ() throws JMSException {
        connection = initializeConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        consumer = initConsumer(topic);
        connection.start();
    }

    private Connection initializeConnection() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        factory.setTrustAllPackages(true);
        Connection NewConnection = factory.createConnection();
        NewConnection.setClientID(clientId);
        return NewConnection;
    }

    private MessageConsumer initConsumer(Topic topic) throws JMSException {
        MessageConsumer newConsumer = session.createDurableSubscriber(topic, subscriptionName);
        newConsumer.setMessageListener(this);
        return newConsumer;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String json = textMessage.getText();
                eventStore.registerProductEvent(topicName, json);
                System.out.println("Producto enviado");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}