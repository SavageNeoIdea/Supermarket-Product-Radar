package org.sni.spr.hiperdino.controller.store;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQStore implements Store {

    private final String url = "tcp://localhost:61616";
    private final Gson gson;

    public ActiveMQStore() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)))
                .create();
    }

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);

        try (Connection connection = factory.createConnection("admin", "admin");
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            connection.start();
            Topic topic = session.createTopic("Products");

            try (MessageProducer producer = session.createProducer(topic)) {
                for (HiperdinoProduct product : productList) {
                    String jsonEvent = wrapProduct(product);
                    producer.send(session.createTextMessage(jsonEvent));
                }
            }
        } catch (JMSException e) {
            throw new RuntimeException("Failed to send products to ActiveMQ", e);
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