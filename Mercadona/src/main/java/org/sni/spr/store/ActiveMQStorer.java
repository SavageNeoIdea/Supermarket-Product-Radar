package org.sni.spr.store;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.sni.spr.model.Product;

import javax.jms.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
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
    public void saveAll(List<Product> products) {
        try {
            Instant batchTs = Instant.now();
            for (Product product : products) {
                String jsonEvent = buildEvent(product, batchTs);
                TextMessage message = session.createTextMessage(jsonEvent);
                message.setStringProperty("eventType", "product");
                message.setStringProperty("source", "mercadona");
                producer.send(message);
            }
            System.out.println("Published " + products.size() + " product events.");
        } catch (JMSException e) {
            throw new RuntimeException("Failed to publish product events", e);
        }
    }

    private String buildEvent(Product product, Instant batchTs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", product.getId());
        payload.put("ean", product.getEan());
        payload.put("brand", product.getBrand());
        payload.put("category", product.getCategory());
        payload.put("subcategory", product.getSubcategory());
        payload.put("name", product.getDisplayName());
        payload.put("qty", product.getUnitSize());
        payload.put("packageQty", product.getTotalUnits());
        payload.put("measure", product.getUnitType());
        payload.put("price", product.getUnitPrice());
        payload.put("gluten", product.getGluten());
        payload.put("urlImage", product.getThumbnail());
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("uid", UUID.randomUUID());
        event.put("ts", batchTs.toString());
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