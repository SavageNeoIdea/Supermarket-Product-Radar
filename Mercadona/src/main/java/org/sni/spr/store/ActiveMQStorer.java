package org.sni.spr.store;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.sni.spr.model.Product;

import javax.jms.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveMQStorer implements Storer, AutoCloseable {
    private final Gson gson;
    private final String brokerUrl;
    private final String topicName;
    private final String username;
    private final String password;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public ActiveMQStorer() {
        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("publishers", "mercadona");
        if (config == null) {
            throw new RuntimeException("ERROR: Could not load configuration for publisher 'mercadona'");
        }
        this.brokerUrl = config.get("brokerUrl");
        this.topicName = config.get("topicName");
        this.username = config.get("username");
        this.password = config.get("password");
        this.gson = new Gson();
        connectToActiveMQ();
    }

    public void connectToActiveMQ() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection(username, password);
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            producer = session.createProducer(topic);
            System.out.println("Connected to ActiveMQ and publisher ready for topic: " + topicName);
        } catch (JMSException e) {
            throw new RuntimeException("Critical ActiveMQ error", e);
        }
    }

    @Override
    public void save(Product product) {
        try {
            Instant ts = Instant.now();
            String jsonEvent = buildEvent(product, ts);
            TextMessage message = session.createTextMessage(jsonEvent);
            message.setStringProperty("eventType", "product");
            message.setStringProperty("source", "mercadona");
            producer.send(message);
            System.out.println("[MQ] Sent product: " + product.getId());
        } catch (JMSException e) {
            throw new RuntimeException("Failed to publish product event", e);
        }
    }

    private String buildEvent(Product product, Instant ts) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mercadonaId", product.getId());
        payload.put("mercadonaEan", product.getEan());
        payload.put("mercadonaBrand", product.getBrand());
        payload.put("mercadonaCategory", product.getCategory());
        payload.put("mercadonaSubcategory", product.getSubcategory());
        payload.put("mercadonaName", product.getDisplayName());
        payload.put("mercadonaQty", product.getUnitSize());
        payload.put("mercadonaPackageQty", product.getTotalUnits());
        payload.put("mercadonaMeasure", product.getUnitType());
        payload.put("mercadonaPrice", product.getUnitPrice());
        payload.put("mercadonaGluten", product.getGluten());
        payload.put("mercadonaUrlImage", product.getThumbnail());
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("uid", UUID.randomUUID());
        event.put("ts", ts.toString());
        event.put("ss", "mercadona");
        event.put("payload", payload);
        return gson.toJson(event);
    }

    @Override
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("ActiveMQ connection closed.");
        } catch (JMSException e) {
            throw new RuntimeException("Failed to close ActiveMQ resources", e);
        }
    }
}