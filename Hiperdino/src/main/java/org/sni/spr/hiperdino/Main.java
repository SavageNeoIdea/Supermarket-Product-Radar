import org.sni.spr.hiperdino.controller.HiperdinoProductSerializer;
import org.sni.spr.hiperdino.controller.Storer;
import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.view.*;

public static void main(String[] args) {
    long startTime = System.currentTimeMillis();

    try {
        HiperdinoProductParser productParser = new HiperdinoProductParser();
        WebScraper webScraper = new HiperdinoPlaywrightManager();
        Storer storer = new HiperdinoProductSerializer();
        Controller controller = new Controller(
                new HiperdinoFeeder(productParser),
                webScraper,
                "35010",
                storer
        );

        controller.init();

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;

        double seconds = durationMillis / 1000.0;
        long minutes = (long) seconds / 60;
        double remainingSeconds = seconds % 60;

        System.out.println("---");
        System.out.println("Proceso finalizado con éxito.");
        System.out.printf("Duración total: %d min y %.2f seg (Total: %.2f seg)%n",
                minutes, remainingSeconds, seconds);
        System.out.println("---");

    } catch (Exception e) {
        System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
        e.printStackTrace();
    }
}
