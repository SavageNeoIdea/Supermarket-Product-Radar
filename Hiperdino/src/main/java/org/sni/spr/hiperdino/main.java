import org.sni.spr.hiperdino.*;

public static void main(String[] args) {

    long startTime = System.currentTimeMillis();

    BrowserManager browserManager = new BrowserManager(false);
    HiperdinoCrawler hiperdinoCrawler = new HiperdinoCrawler("35010", browserManager);
    HiperdinoScraper hiperdinoScraper = new HiperdinoScraper(browserManager, hiperdinoCrawler);

    Initer initer = new Initer(browserManager, hiperdinoCrawler, hiperdinoScraper);
    Map<String, List<Product>> Products = initer.initAndExtractProductsMap();

    System.out.println("--- LISTADO DE PRODUCTOS ENCONTRADOS ---");

    Products.forEach((name, data) -> {
        System.out.println("Categoría - Subcategoría: " + name);
        System.out.println("Productos:\n" + data);
        System.out.println("---------------------------------------");
    });

    long endTime = System.currentTimeMillis();

    double durationInSeconds = (endTime - startTime) / 1000.0;
    System.out.println("Tiempo total: " + durationInSeconds + " segundos");
}
