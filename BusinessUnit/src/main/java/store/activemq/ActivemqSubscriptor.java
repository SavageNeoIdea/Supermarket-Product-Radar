package store.activemq;

import controller.feeder.Feeder;
import store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.jms.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivemqSubscriptor implements MessageListener, Subscriptor {

    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;

    private String brokerUrl;
    private String topicName;
    private String clientId;
    private String subscriptionName;
    private String username;
    private String password;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);

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
    public synchronized void start() {
        if (running.get()) {
            logInfo("Subscriber already started.");
            return;
        }

        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setTrustAllPackages(true);

            this.connection = createJmsConnection(factory);
            if (clientId != null && !clientId.isBlank()) {
                this.connection.setClientID(clientId);
            }

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            this.consumer = session.createDurableSubscriber(topic, subscriptionName);
            this.consumer.setMessageListener(this);

            this.connection.start();
            running.set(true);
            logInfo("BusinessUnit subscriber started successfully.");
        } catch (JMSException e) {
            logError("JMS error initializing subscriber: " + e.getMessage());
            safeCloseResources();
        }
    }

    @Override
    public synchronized void close() {
        if (!running.getAndSet(false)) {
            logInfo("Subscriber already stopped or not started.");
            return;
        }

        if (connection != null) {
            try {
                connection.stop();
                logInfo("JMS connection stopped.");
            } catch (JMSException e) {
                logError("Error stopping JMS connection: " + e.getMessage());
            }
        }

        if (consumer != null) {
            try {
                consumer.close();
                logInfo("MessageConsumer closed.");
            } catch (JMSException e) {
                logError("Error closing MessageConsumer: " + e.getMessage());
            } finally {
                consumer = null;
            }
        }

        if (session != null) {
            try {
                session.close();
                logInfo("JMS Session closed.");
            } catch (JMSException e) {
                logError("Error closing JMS Session: " + e.getMessage());
            } finally {
                session = null;
            }
        }

        if (connection != null) {
            try {
                connection.close();
                logInfo("JMS Connection closed.");
            } catch (JMSException e) {
                logError("Error closing JMS Connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    private void safeCloseResources() {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException ignored) {}
        consumer = null;

        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException ignored) {}
        session = null;

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ignored) {}
        connection = null;

        running.set(false);
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
            logError("Unexpected error processing message in ActivemqSubscriptor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processIncomingMessage(TextMessage textMessage) throws Exception {
        String event = textMessage.getText();
        String source = extractSourceFromJson(event);
        if (source == null) {
            logError("WARN: Se recibió un evento pero no se pudo determinar el 'ss' en el JSON.");
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
            logError("Error parseando el JSON para extraer 'ss': " + e.getMessage());
        }
        return null;
    }

    private void logInfo(String msg) {
        System.out.println("INFO: " + msg);
    }
    private void logError(String msg) {
        System.err.println("ERROR: " + msg);
    }
}
