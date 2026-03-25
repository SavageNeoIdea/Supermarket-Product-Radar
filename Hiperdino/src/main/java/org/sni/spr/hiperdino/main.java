import org.sni.spr.hiperdino.HiperdinoScraper;

public static void main(String[] args) {
    HiperdinoScraper scraper = new HiperdinoScraper("35010");
    scraper.init();
    scraper.manageCookies();
    scraper.writePostalCode();
    scraper.close();
}
