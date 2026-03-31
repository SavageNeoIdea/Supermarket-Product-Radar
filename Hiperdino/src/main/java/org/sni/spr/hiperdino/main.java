import org.sni.spr.hiperdino.HiperdinoScraper;

public static void main(String[] args) {
    HiperdinoScraper scraper = new HiperdinoScraper("35010");
    scraper.init();
    scraper.manageCookies();
    scraper.writePostalCode();
    Map<String, String> categorias = scraper.extractCategories();

    System.out.println("--- LISTADO DE CATEGORÍAS ENCONTRADAS ---");

    categorias.forEach((nombre, url) -> {
        System.out.println("Categoría: " + nombre);
        System.out.println("Enlace:    " + url);
        System.out.println("---------------------------------------");
    });

    System.out.println("Total: " + categorias.size() + " categorías.");
    scraper.close();

}
