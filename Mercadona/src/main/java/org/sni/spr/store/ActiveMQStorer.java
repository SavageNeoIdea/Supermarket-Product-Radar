package org.sni.spr.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.sni.spr.model.Product;

import javax.jms.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActiveMQStorer implements Storer, AutoCloseable {
    private static final String TOPIC_NAME = "events.product";
    private final Gson gson;
    private final String source;
    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public ActiveMQStorer(String brokerUrl,
                         String user,
                         String password,
                         String source) {
        this.source = source;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, _, _) ->
                                new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME))
                )
                .create();
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection(user, password);
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);
            this.producer = session.createProducer(topic);
            System.out.println("Connected to ActiveMQ broker: " + brokerUrl);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to initialize ActiveMQ connection", e);
        }
    }

    @Override
    public void saveAll(List<Product> products) {
        try {
            for (Product product : products) {
                String jsonEvent = buildEvent(product);
                TextMessage message = session.createTextMessage(jsonEvent);
                message.setStringProperty("eventType", "product");
                message.setStringProperty("source", source);
                producer.send(message);
            }
            System.out.println("Published " + products.size() + " product events.");
        } catch (JMSException e) {
            throw new RuntimeException("Failed to publish product events", e);
        }
    }

    private String buildEvent(Product product) {
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
        event.put("ts", Instant.now().toString());
        event.put("ss", source);
        event.put("payload", payload);
        return gson.toJson(event);
    }

    @Override
    public void close() {
        try {
            producer.close();
            session.close();
            connection.close();
            System.out.println("ActiveMQ connection closed.");
        } catch (JMSException e) {
            throw new RuntimeException("Failed to close ActiveMQ resources", e);
        }
    }
}