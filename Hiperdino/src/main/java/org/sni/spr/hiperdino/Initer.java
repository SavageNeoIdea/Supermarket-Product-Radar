package org.sni.spr.hiperdino;

import java.util.List;
import java.util.Map;


public class Initer{

    private final HiperdinoCrawler hiperdinoCrawler;
    private final HiperdinoScraper hiperdinoScraper;
    private final BrowserManager browserManager;

    private Map<String, List<String>> categoriesMap;

    public Initer(BrowserManager browserManager,
                  HiperdinoCrawler hiperdinoCrawler, HiperdinoScraper hiperdinoScraper){
        this.browserManager = browserManager;
        this.hiperdinoCrawler = hiperdinoCrawler;
        this.hiperdinoScraper = hiperdinoScraper;
    }
    public void prepareSession(){
        browserManager.init();
        hiperdinoCrawler.prepareSession();
    }

    public Map<String, List<Product>> initAndExtractProductsMap() {

        hiperdinoCrawler.extractCategoriesUrls();

        System.out.println("--- LISTADO DE CATEGORÍAS ENCONTRADAS ---");

        hiperdinoCrawler.getCategoriesMap().forEach((name, data) -> {
            System.out.println("Categoría - Subcategoría: " + name);
            System.out.println("Productos:\n" + data.getFirst());
            System.out.println("---------------------------------------");
        });

        HiperdinoScraper hiperdinoScraper = new HiperdinoScraper(browserManager, hiperdinoCrawler);
        hiperdinoScraper.extractAllProducts();

        browserManager.close();
        return hiperdinoScraper.getProductsMap();
    }
}