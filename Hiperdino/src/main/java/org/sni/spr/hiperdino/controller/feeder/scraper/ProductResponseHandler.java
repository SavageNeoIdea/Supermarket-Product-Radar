package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ProductResponseHandler {
    private final Consumer<String> onJsonCaptured;
    private final Page page;
    private String subcategory;
    private final List<String> capturedResponses = new ArrayList<>();

    public ProductResponseHandler(Page page, String url, Consumer<String> rawDataConsumer) {
        this.page = page;
        this.onJsonCaptured = rawDataConsumer;
        setupNetworkInterceptor();
        scrollUntilEnd();
    }

    public void setupNetworkInterceptor() {
        page.onResponse(this::handleResponse);
    }

    public void scrollUntilEnd() {
        int failureAttempts = 0;
        final int maxFailures = 6;
        while (failureAttempts < maxFailures) {
            int previousCount = countProducts();
            simulateHumanScroll();
            failureAttempts = (pageHasNewProducts(previousCount)) ? 0 : ++failureAttempts;
        }
    }

    private void handleResponse(Response response) {
        String url = response.url();
        if (url.contains(subcategory + ".html") && url.contains("is_scroll=1")) {
            try {
                if (response.status() == 200) {
                    String responseBody = response.text();
                    onJsonCaptured.accept(responseBody);
                    System.out.println("JSON de scroll enviado al pipeline");
                }
            } catch (Exception e) {
                System.err.println("Error al capturar respuesta: " + e.getMessage());
            }
        }
    }

    private boolean pageHasNewProducts(int previousCount) {
        return countProducts() > previousCount;
    }

    private int countProducts() {
        return page.locator(".product-list-item.flex-item.loader-over").count();
    }

    private void simulateHumanScroll() {
        int totalScroll = ThreadLocalRandom.current().nextInt(400, 1201);
        int currentScroll = 0;

        while (currentScroll < totalScroll) {
            int step = ThreadLocalRandom.current().nextInt(50, 151);
            scrollPage(step);
            currentScroll += step;
            page.waitForTimeout(ThreadLocalRandom.current().nextInt(50, 151));
        }
        stochasticWait();
    }

    private void stochasticWait() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        if (ThreadLocalRandom.current().nextDouble() > 0.7) {
            int randomMin = ThreadLocalRandom.current().nextInt(5000, 7000);
            int randomMax = ThreadLocalRandom.current().nextInt(7000, 1200);
            humanWait(randomMin, randomMax);
        }
    }

    private void humanWait(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(min, max + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void scrollPage(int height) {
        page.mouse().wheel(0, height);
    }

    public List<String> getCapturedResponses() {
        return capturedResponses;
    }

}
