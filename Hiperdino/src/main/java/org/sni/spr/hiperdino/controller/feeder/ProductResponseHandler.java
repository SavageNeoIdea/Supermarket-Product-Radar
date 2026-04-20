package org.sni.spr.hiperdino.controller.feeder;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ProductResponseHandler {
    private Page page;
    private String subcategory;
    private final List<String> capturedResponses = new ArrayList<>();

    public ProductResponseHandler(Page page, String subcategory) {
        this.page = page;
        this.subcategory = subcategory;
        setupNetworkInterceptor();
        scrollUntilEnd();
    }

    public void setupNetworkInterceptor() {
        page.onResponse(this::handleResponse);
    }

    private void handleResponse(Response response) {
        String url = response.url();

        if (url.contains(subcategory + ".html") && url.contains("is_scroll=1")) {
            try {
                if (response.status() == 200) {
                    String responseBody = response.text();
                    capturedResponses.add(responseBody);
                    System.out.println("✅ Respuesta capturada exitosamente: " + url);
                }
            } catch (Exception e) {
                System.err.println("Error al leer la respuesta de " + url + ": " + e.getMessage());
            }
        }
    }

    public List<String> getCapturedResponses() {
        return capturedResponses;
    }

    public void scrollUntilEnd() {
        String productSelector = ".product-list-item.flex-item.loader-over";
        int attempts = 0;
        int maxAttempts = 6;

        while (attempts < maxAttempts) {
            int currentCount = page.locator(productSelector).count();
            simulateHumanScroll();

            page.waitForLoadState(LoadState.NETWORKIDLE);
            if (Math.random() > 0.7) {humanWait(3000, 5000);}

            int newCount = page.locator(productSelector).count();

            if (newCount > currentCount) {attempts = 0;}
            else {
                page.mouse().wheel(0, 500);
                attempts++;
                chargeNewRequests();
            }
        }
    }

    private void simulateHumanScroll() {
        int totalScroll = (int) (Math.random() * 800 + 400);
        int currentScroll = 0;

        while (currentScroll < totalScroll) {
            int step = (int) (Math.random() * 100 + 50);
            page.mouse().wheel(0, step);
            currentScroll += step;
            page.waitForTimeout((int) (Math.random() * 100 + 50));
        }
    }

    private void humanWait(int min, int max) {
        try {
            long sleepTime = ThreadLocalRandom.current().nextLong(min, max + 1);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error en la espera aleatoria", e);
        }
    }

    private void chargeNewRequests() {
        for (int i = 0; i < 3; i++) {
            int scrollAmount = ThreadLocalRandom.current().nextInt(400, 900);
            scrollPage(scrollAmount);
            humanWait(400, 800);
        }
    }

    private void scrollPage(int height) {page.mouse().wheel(0, height);}

}
