package org.sni.businessunit.controller.activemq;

import jakarta.jms.*;
import java.util.function.Consumer;

public class ActiveMqSubscriptor implements MessageListener, Subscriptor {

    private final ActiveMqConfig activeMqConfig;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private boolean isRunning = false;
    private Consumer<String> onMessageCallback;

    public ActiveMqSubscriptor(ActiveMqConfig activeMqConfig) {
        this.activeMqConfig = activeMqConfig;
    }

    @Override
    public synchronized void start(Consumer<String> onMessageCallback){
        if (subscriptionIsRunning()) {
            logInfo("Subscriber already started.");
            return;
        }
        this.onMessageCallback = onMessageCallback;
        try {
            connection = ActiveMqConnectionFactory.createConnection(activeMqConfig.brokerUrl(),
                    activeMqConfig.clientId(),activeMqConfig.username(), activeMqConfig.password());
            session = createNewSession(connection);
            Topic topic = session.createTopic(activeMqConfig.topicName());
            consumer = session.createDurableSubscriber(topic, activeMqConfig.subscriptionName());
            consumer.setMessageListener(this);
            connection.start();
            isRunning = true;
            logInfo("BusinessUnit subscriber started successfully.");
        } catch (JMSException e) {
            logError("JMS error initializing subscriber: " + e.getMessage());
            close();
        }
    }

    private boolean subscriptionIsRunning(){
        return isRunning;
    }

    private Session createNewSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public synchronized void close() {
        if (!isRunning) return;

        closeQuietly(consumer, "Consumer");
        closeQuietly(session, "Session");
        closeQuietly(connection, "Connection");

        isRunning = false;
        logInfo("Subscriber closed and resources released.");
    }

    private void closeQuietly(AutoCloseable resource, String name) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                logError("Error closing " + name + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                onMessageCallback.accept(textMessage.getText());
            }
        } catch (Exception e) {
            logError("Unexpected error processing message in ActivemqSubscriptor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logInfo(String msg) {
        System.out.println("INFO: " + msg);
    }

    private void logError(String msg) {
        System.err.println("ERROR: " + msg);
    }
}
