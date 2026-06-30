package org.sni.businessunit.controller.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;

public class ActiveMqConnectionFactory {

    public static Connection createConnection(String brokerUrl, String clientId, String username, String password) throws JMSException {
        ActiveMQConnectionFactory factory = new org.apache.activemq.ActiveMQConnectionFactory(brokerUrl);
        factory.setTrustAllPackages(true);
        Connection newConnection = createJmsConnection(factory, username, password);
        if (clientIdIsValid(clientId))
            newConnection.setClientID(clientId);
        return newConnection;
    }

    private static Connection createJmsConnection(ActiveMQConnectionFactory factory, String username, String password) throws JMSException {
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            return factory.createConnection(username, password);
        }
        return factory.createConnection();
    }

    private static boolean clientIdIsValid(String clientId) {
        return clientId != null && !clientId.isBlank();
    }
}
