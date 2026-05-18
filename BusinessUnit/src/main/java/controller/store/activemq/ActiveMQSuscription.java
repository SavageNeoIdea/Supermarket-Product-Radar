package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ActiveMQSuscription implements MessageListener, Subscriptor {

    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;
    public ActiveMQSuscription(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;
    }
    @Override
    public void start() {
        try {
            Map<String, String> config = loadConfig("businessUnitSubscriber");
            if (config.isEmpty()) {
                System.err.println("ERROR: No se pudo encontrar o parsear la configuración de businessUnitSubscriber.");
                return;
            }
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(config.get("brokerUrl"));
            factory.setTrustAllPackages(true);
            Connection connection;
            String user = config.get("username");
            String pass = config.get("password");
            if (user != null && !user.isEmpty() && !user.equals("null") &&
                    pass != null && !pass.isEmpty() && !pass.equals("null")) {
                connection = factory.createConnection(user, pass);
            } else {
                connection = factory.createConnection();
            }
            connection.setClientID(config.get("clientId"));
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(config.get("topicName"));
            MessageConsumer consumer = session.createDurableSubscriber(topic, config.get("subscriptionName"));
            consumer.setMessageListener(this);
            connection.start();
            System.out.println("INFO: Suscriptor ActiveMQ para BusinessUnit iniciado con éxito desde config.json.");
        } catch (JMSException e) {
            System.err.println("Error de JMS al inicializar el suscriptor ActiveMQ: " + e.getMessage());
        }
    }

    private Map<String, String> loadConfig(String subscriberKey) {
        Map<String, String> configMap = new HashMap<>();
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            configFile = new File("../config.json");
        }
        if (!configFile.exists()) {
            System.err.println("ERROR CRÍTICO: No se encontró el archivo config.json en la ruta: " + configFile.getAbsolutePath());
            return configMap;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
            String content = contentBuilder.toString();
            if (content.contains("\"" + subscriberKey + "\"")) {
                String afterKey = content.split("\"" + subscriberKey + "\"")[1];
                String block = afterKey.substring(afterKey.indexOf("{") + 1, afterKey.indexOf("}"));
                String[] pairs = block.split(",");
                for (String pair : pairs) {
                    if (pair.contains(":")) {
                        String[] keyValue = pair.split(":", 2);
                        String key = keyValue[0].replace("\"", "").trim();
                        String value = keyValue[1].replace("\"", "").trim();
                        if (value.equalsIgnoreCase("null")) {
                            value = null;
                        }
                        configMap.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo físico config.json: " + e.getMessage());
        }
        return configMap;
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
        } catch (JMSException e) {
            System.err.println("Error de JMS al recibir el mensaje en tiempo real");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado en el hilo de escucha (onMessage)");
            e.printStackTrace();
        }
    }
}