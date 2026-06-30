package org.sni.businessunit.controller.activemq;

import java.util.function.Consumer;

public interface Subscriptor {
    void start(Consumer<String> onMessageReceived);
    void close();
}
