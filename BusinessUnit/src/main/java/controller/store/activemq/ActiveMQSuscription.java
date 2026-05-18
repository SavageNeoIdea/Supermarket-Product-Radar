package controller.store.activemq;

import controller.feeder.Feeder;
import controller.store.DatamartStore;
import model.Product;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public class ActiveMQSuscription implements MessageListener, Subscriptor {

    private final String brokerUrl;
    private final String topicName;
    private final String clientId;
    private final String subscriptionName;
    private final Feeder dataPreprocessor;
    private final DatamartStore datamartStore;

    public ActiveMQSuscription(String brokerUrl,
                               String topicName,
                               String clientId,
                               String subscriptionName,
                               Feeder dataPreprocessor,
                               DatamartStore datamartStore) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientId = clientId;
        this.subscriptionName = subscriptionName;
        this.dataPreprocessor = dataPreprocessor;
        this.datamartStore = datamartStore;
    }

    public ActiveMQSuscription(Feeder dataPreprocessor, DatamartStore datamartStore) {
        this(
                "tcp://localhost:61616",
                "product",
                "EventStoreBuilder_Subscriber",
                "MainEventStoreSub",
                dataPreprocessor,
                datamartStore
        );
    }

    public ActiveMQSuscription(String brokerUrl, Feeder dataPreprocessor, DatamartStore datamartStore) {
        this(
                brokerUrl,
                "product",
                "EventStoreBuilder_Subscriber",
                "MainEventStoreSub",
                dataPreprocessor,
                datamartStore
        );
    }

    public ActiveMQSuscription(String brokerUrl, String topicName, Feeder dataPreprocessor, DatamartStore datamartStore) {
        this(
                brokerUrl,
                topicName,
                "EventStoreBuilder_Subscriber",
                "MainEventStoreSub",
                dataPreprocessor,
                datamartStore
        );
    }

    public ActiveMQSuscription(String brokerUrl, String topicName, String clientId, Feeder dataPreprocessor, DatamartStore datamartStore) {
        this(
                brokerUrl,
                topicName,
                clientId,
                "MainEventStoreSub",
                dataPreprocessor,
                datamartStore
        );
    }

    @Override
    public void start() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setTrustAllPackages(true);
            Connection connection = factory.createConnection();
            connection.setClientID(clientId);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
            consumer.setMessageListener(this);
            connection.start();
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