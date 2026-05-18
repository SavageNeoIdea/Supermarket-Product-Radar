package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.jms.*;
import java.util.List;
import java.util.Map;

public class ActivemqSubscriptor implements MessageListener, Subscriptor {

    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;

    private String brokerUrl;
    private String topicName;
    private String clientId;
    private String subscriptionName;
    private String username;
    private String password;

    public ActivemqSubscriptor(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;

        loadModuleConfiguration();
    }

    private void loadModuleConfiguration() {
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
            Connection connection = createJmsConnection(factory);
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

    private Connection createJmsConnection(ActiveMQConnectionFactory factory) throws JMSException {
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            return factory.createConnection(username, password);
        }
        return factory.createConnection();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                processIncomingMessage(textMessage);
            }
        } catch (Exception e) {
            System.err.println("Unexpected error processing message in ActivemqSubscriptor");
            e.printStackTrace();
        }
    }

    // SEGMENTACIÓN: Extrae el evento, descubre el 'ss' del JSON y alimenta al preprocesador
    private void processIncomingMessage(TextMessage textMessage) throws Exception {
        String event = textMessage.getText();
        String source = extractSourceFromJson(event);
        if (source == null) {
            System.err.println("WARN: Se recibió un evento pero no se pudo determinar el 'ss' en el JSON.");
            return;
        }
        Product product = dataPreprocessor.processData(source, event);
        if (product != null) {
            datamartStore.storeAllData(List.of(product));
        }
    }

    private String extractSourceFromJson(String eventString) {
        try {
            JsonObject root = JsonParser.parseString(eventString).getAsJsonObject();
            if (root.has("ss") && !root.get("ss").isJsonNull()) {
                return root.get("ss").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error parseando el JSON para extraer 'ss': " + e.getMessage());
        }
        return null;
    }
}