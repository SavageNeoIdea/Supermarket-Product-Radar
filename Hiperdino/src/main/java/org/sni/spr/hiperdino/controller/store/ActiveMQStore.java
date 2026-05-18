package org.sni.spr.hiperdino.controller.store;
import com.google.gson.Gson;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActiveMQStore implements Store {

    private final Gson gson;
    private final String brokerUrl;
    private final String topicName;
    private final String username;
    private final String password;

    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageProducer producer;

    public ActiveMQStore() {

        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("publishers", "hiperdino");

        if (config == null) {
            throw new RuntimeException("ERROR: Could not load configuration for publisher 'hiperdino'");
        }

        this.brokerUrl = config.get("brokerUrl");
        this.topicName = config.get("topicName");
        this.username = config.get("username");
        this.password = config.get("password");
        this.gson = new Gson();
    }

    @Override
    public synchronized void storeSingleData(HiperdinoProduct product) {
        try {
            if (session == null) connectToActiveMQ();
            String jsonEvent = wrapProduct(product);
            producer.send(session.createTextMessage(jsonEvent));
        } catch (JMSException e) {
            System.err.println("JMS send failure: " + e.getMessage());
            session = null;
        }
    }

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        productList.forEach(this::storeSingleData);
    }

    public void connectToActiveMQ() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection(username, password);
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic(topicName);
            producer = session.createProducer(topic);
            System.out.println("Connected to ActiveMQ and publisher ready for topic: " + topicName);
        } catch (JMSException e) {
            throw new RuntimeException("Critical ActiveMQ error", e);
        }
    }

    @Override
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String wrapProduct(HiperdinoProduct product) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("hiperdinoSku", product.getHiperdinoSku());
        payload.put("hiperdinoEan", product.getHiperdinoEan());
        payload.put("hiperdinoBrand", product.getHiperdinoBrand());
        payload.put("hiperdinoCategory", product.getHiperdinoCategory());
        payload.put("hiperdinoSubcategory", product.getHiperdinoSubcategory());
        payload.put("hiperdinoName", product.getHiperdinoName());
        payload.put("hiperdinoQty", product.getHiperdinoQty());
        payload.put("hiperdinoPackageQty", product.getHiperdinoPackageQty());
        payload.put("hiperdinoMeasure", product.getHiperdinoMeasure());
        payload.put("hiperdinoPrice", product.getHiperdinoPrice());
        payload.put("hiperdinoGluten", product.getHiperdinoGluten());
        payload.put("hiperdinoUrlImage", product.getHiperdinoUrlImage());
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("uid", product.getHiperdinoEventId());
        event.put("ts", product.getHiperdinoTs().toString());
        event.put("ss", "hiperdino");
        event.put("payload", payload);
        return gson.toJson(event);
    }
}