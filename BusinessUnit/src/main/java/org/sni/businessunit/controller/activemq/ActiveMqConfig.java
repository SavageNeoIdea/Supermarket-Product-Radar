package org.sni.businessunit.controller.activemq;

import java.util.Map;

public class ActiveMqConfig {
    private String brokerUrl;
    private String topicName;
    private String clientId;
    private String subscriptionName;
    private String username;
    private String password;

    public ActiveMqConfig() {}

    public void initConfigurations(){
        loadModuleConfiguration();
    }

    private void loadModuleConfiguration() {
        ConfigReader reader = new ConfigReader();
        Map<String, String> config = reader.loadConfig("subscribers", "businessUnitSubscriber");
        if (config == null) {
            throw new RuntimeException("ERROR: Could not load configuration for businessUnitSubscriber");
        }
        this.brokerUrl = config.get("brokerUrl");
        this.topicName = config.get("topicName");
        this.clientId = config.get("clientId");
        this.subscriptionName = config.get("subscriptionName");
        this.username = config.get("username");
        this.password = config.get("password");
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
