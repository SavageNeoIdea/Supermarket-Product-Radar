package model;

public class Publisher {
    private final String topic;
    private final String source;

    public Publisher(String topic, String source) {
        this.topic = topic;
        this.source = source;
    }

    public String getTopic() {
        return topic;
    }

    public String getSource() {
        return source;
    }
}
