package controller.store.activemq;

import controller.feeder.Feeder;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQSuscription implements MessageListener, Subscriptor {

    private final String BROKER_URL = "tcp://localhost:61616";
    private final String TOPIC_NAME = "product";
    private final String CLIENT_ID = "EventStoreBuilder_Subscriber";
    private final String SUBSCRIPTION_NAME = "MainEventStoreSub";

    private Connection connection;
    private Session session;
    private Feeder dataPreprocessor;

    public ActiveMQSuscription(Feeder dataPreprocessor) {
        this.dataPreprocessor = dataPreprocessor;
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
                Product product =
                        dataPreprocessor.processData(source, event);
                //todo: actualmente no estoy seguro de como usar los mensajes para aportar valor
                // a la aplicación por ahora solo se calcula el product y ya.
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}