package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;
import java.util.Map;

public class ActiveMQSuscription implements MessageListener, Subscriptor {

    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;
    public ActiveMQSuscription(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;
    }
    @Override
    public void start() {
        ConfigReader configReader = new ConfigReader();
        try {
            Map<String, String> config = configReader.loadConfig("subscribers","businessUnitSubscriber");
            if (config == null) {
                return;
            }
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(config.get("brokerUrl"));
            factory.setTrustAllPackages(true);
            Connection connection;
            String user = config.get("username");
            String pass = config.get("password");
            if (user != null && !user.isEmpty() && pass != null && !pass.isEmpty()) {
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