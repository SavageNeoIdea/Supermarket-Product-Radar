public class Main {
    public static void main(String[] args) {
        EventStore eventStore = new EventStore();
        Suscriptor suscriptor = new Suscriptor(eventStore);
        suscriptor.start();
    }
}