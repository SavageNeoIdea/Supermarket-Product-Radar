package org.sni.spr.hiperdino.controller.store;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQStore implements Store {

    private final Gson gson;
    private final String user;
    private final String url;
    private final String password;
    private Connection connection;
    private Session session;
    private Topic topic;

    public ActiveMQStore(String url, String user, String password) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)))
                .create();
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        connectToActiveMQ();
        try (MessageProducer producer = session.createProducer(topic)) {
            for (HiperdinoProduct product : productList) {
                String jsonEvent = wrapProduct(product);
                producer.send(session.createTextMessage(jsonEvent));
            }
            System.out.println("Sent " + productList.size() + " products to broker.");
        } catch (JMSException e) {
            throw new RuntimeException("Failed to send products", e);
        }
    }

    public void connectToActiveMQ() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
            connection = factory.createConnection(user, password);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("Product");

            System.out.println("Connected to ActiveMQ: " + url);
        } catch (JMSException e) {
            throw new RuntimeException("Could not establish ActiveMQ connection", e);
        }
    }

    private String wrapProduct(HiperdinoProduct product) {
        Map<String, Object> event = new HashMap<>();
        event.put("ts", LocalDateTime.now().toString());
        event.put("ss", "Hiperdino");
        event.put("payload", product);
        return gson.toJson(event);
    }
}