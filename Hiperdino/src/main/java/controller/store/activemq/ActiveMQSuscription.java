package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public class ActiveMQSuscription implements MessageListener, Subscriptor {

    private final String BROKER_URL = "tcp://localhost:61616";
    private final String TOPIC_NAME = "product";
    private final String CLIENT_ID = "EventStoreBuilder_Subscriber";
    private final String SUBSCRIPTION_NAME = "MainEventStoreSub";

    private Connection connection;
    private Session session;
    private Feeder dataPreprocessor;
    private final DatamartStore datamartStore;

    public ActiveMQSuscription(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;
    }

    @Override
    public void start() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            factory.setTrustAllPackages(true);

            connection = factory.createConnection();
            connection.setClientID(CLIENT_ID);

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);

            MessageConsumer consumer = session.createDurableSubscriber(topic, SUBSCRIPTION_NAME);
            consumer.setMessageListener(this);

            connection.start();
            System.out.println("BussinesUnit: Suscrito al topic 'Products'. Esperando eventos...");

        } catch (JMSException e) {
            System.err.println("Error en el Suscriptor: " + e.getMessage());
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
            System.err.println("Error de JMS al recibir el mensaje de datos en vivo");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado procesando el mensaje en vivo");
            e.printStackTrace();
        }
    }
}