package org.sni.businessunit.controller.activemq;

public record ActiveMqConfig(String brokerUrl, String topicName, String clientId, String subscriptionName,
                             String username, String password) {
}
