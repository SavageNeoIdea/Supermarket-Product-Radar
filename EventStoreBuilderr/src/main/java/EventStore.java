public class EventStore {

    public void saveEvent(String data) {

        System.out.println("[\u2705 EventStore] Guardando evento en el registro: " + data);
        persistToDisk(data);
    }

    private void persistToDisk(String data) {
    }
}