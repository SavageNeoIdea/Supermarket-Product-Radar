package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;
import java.util.Map;

public class ActivemqSubscriptor implements MessageListener, Subscriptor {

    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;

    private final String brokerUrl;
    private final String topicName;
    private final String clientId;
    private final String subscriptionName;
    private final String username;
    private final String password;

    public ActivemqSubscriptor(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;

        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("subscribers", "businessUnitSubscriber");
        if (config == null) {
            throw new RuntimeException("ERROR: Could not load configuration for businessUnitSubscriber");
        }
        this.brokerUrl = config.get("brokerUrl");
        this.topicName = config.get("topicName");
        this.clientId = config.get("clientId");
        this.subscriptionName = config.get("subscriptionName");
        this.username = config.get("username");
        this.password = config.get("password");
    }

    @Override
    public void start() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setTrustAllPackages(true);
            Connection connection;
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                connection = factory.createConnection(username, password);
            } else {
                connection = factory.createConnection();
            }
            connection.setClientID(clientId);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
            consumer.setMessageListener(this);
            connection.start();
            System.out.println("INFO: BusinessUnit subscriber started successfully.");

        } catch (JMSException e) {
            System.err.println("JMS error initializing subscriber: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String event = textMessage.getText();
                String source = message.getStringProperty("ss");
                Product product = dataPreprocessor.processData(source, event);
                if (product != null) {
                    datamartStore.storeAllData(List.of(product));
                }
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in onMessage");
            e.printStackTrace();
        }
    }
}
