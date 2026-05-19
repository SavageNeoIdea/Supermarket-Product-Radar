package org.sni.spr.hiperdino.controller.feeder.scraper;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ProductResponseHandler {
    private final Consumer<String> onJsonCaptured;
    private final Page page;
    private final String subcategory;
    private final Set<Integer> seenResponseHashes = new HashSet<>();

    public ProductResponseHandler(Page page, String subcategory, Consumer<String> rawDataConsumer) {
        this.page = page;
        this.onJsonCaptured = rawDataConsumer;
        this.subcategory = subcategory;
        setupNetworkInterceptor();
    }

    public void setupNetworkInterceptor() {
        page.onResponse(this::handleResponse);
    }

    private void handleResponse(Response response) {
        String url = response.url();
        if (subcategory != null && url.contains(subcategory + ".html") && url.contains("is_scroll=1")) {
            try {
                if (response.status() == 200) {
                    String responseBody = response.text();
                    int contentHash = responseBody.hashCode();
                    if (seenResponseHashes.contains(contentHash)) {
                        return;
                    }
                    seenResponseHashes.add(contentHash);
                    onJsonCaptured.accept(responseBody);
                    System.out.println("JSON de scroll enviado al pipeline (Nuevo lote capturado)");
                }
            } catch (Exception e) {
                System.err.println("Error al capturar respuesta: " + e.getMessage());
            }
        }
    }
}
