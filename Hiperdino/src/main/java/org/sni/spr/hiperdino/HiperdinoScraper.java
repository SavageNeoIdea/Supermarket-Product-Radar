package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class HiperdinoScraper {


    private final Map<String,List<Product>> productsMap;
    private final BrowserManager browserManager;
    private final HiperdinoCrawler hiperdinoCrawler;
    private List<Product> productList;


    public HiperdinoScraper(BrowserManager browserManager, HiperdinoCrawler hiperdinoCrawler){
        this.browserManager = browserManager;
        this.hiperdinoCrawler = hiperdinoCrawler;
        this.productsMap = new LinkedHashMap<>();
    }

    public void extractAllProducts(){
        for (Map.Entry<String, List<String>> entry : hiperdinoCrawler.getCategoriesMap().entrySet()) {

            List<String> categoryItem = entry.getValue();
            extractCategoryProducts(categoryItem.getFirst());

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void extractCategoryProducts(String url){
        this.productList.clear();
        browserManager.navigateTo(url);
        List<String> currentCategory = UrlParser.getFormatedData(UrlParser.initPattern().matcher(url), url);
        String name = currentCategory.get(0);
        String category = currentCategory.get(2);
        String subcategory = currentCategory.get(3);
        browserManager.chargeProductsHtml(2500);

        List<Locator> productLocation = browserManager.getPage().
                locator(".product-list-item.flex-item.loader-over").all();

        for (Locator product : productLocation){
            registerProduct(category, subcategory, product);
        }

        productsMap.put(name, productList);
    }

    private void registerProduct(String category, String subcategory, Locator product){
        ProductIdentifier identifier = new ProductIdentifier(
                product.locator(".description__text.name").innerText().trim(),
                product.locator(".price__text.price").innerText().trim());

        String name = identifier.getName();
        int qty = identifier.getQty();
        UnitsOfMeasurement measure = identifier.getMeasure();
        double price = identifier.getPrice();

        boolean gluten = product.locator(".badge__text.text--semi-bold")
                .filter(new Locator.FilterOptions().setHasText("Sin gluten"))
                .isVisible();

        String urlImage = product.locator("img.image--wrapper").getAttribute("src");
        productList.add(new Product(category,subcategory,name,qty, measure, price, gluten, urlImage));
    }

    public Map<String, List<Product>> getProductsMap() {return productsMap;}

}
