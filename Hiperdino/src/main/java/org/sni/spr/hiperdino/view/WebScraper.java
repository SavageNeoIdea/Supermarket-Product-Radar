package org.sni.spr.hiperdino.view;
import com.microsoft.playwright.Page;
import java.util.List;
import java.util.Map;

public interface WebScraper {
    public void init(String postalCode);
    public void navigateTo(String url);
    public Page getPage();
    public void click(String buttonLocation);
    public void fill(String inputLocation, String data);
    public void scrollPage(int height);
    public void close();
    public  Map<String, List<Map<String, String>>> extractProductRawData();
}
