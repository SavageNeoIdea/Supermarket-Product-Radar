package org.sni.spr.hiperdino.controller.store;
import com.google.gson.Gson;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQStore implements Store {
    private final Gson gson;
    private final String user, url, password;
    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageProducer producer;


    public ActiveMQStore(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.gson = new Gson();
    }

    @Override
    public synchronized void storeSingleData(HiperdinoProduct product) {
        try {
            if (session == null) connectToActiveMQ();
            String jsonEvent = wrapProduct(product);
            producer.send(session.createTextMessage(jsonEvent));
        } catch (JMSException e) {
            System.err.println("Fallo en el envío JMS: " + e.getMessage());
            session = null;
        }
    }

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
        productList.forEach(this::storeSingleData);
    }

    public void connectToActiveMQ() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
            connection = factory.createConnection(user, password);
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("product");
            producer = session.createProducer(topic);
            System.out.println("Conectado y Productor listo en ActiveMQ");
        } catch (JMSException e) {
            throw new RuntimeException("Error crítico en ActiveMQ", e);
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