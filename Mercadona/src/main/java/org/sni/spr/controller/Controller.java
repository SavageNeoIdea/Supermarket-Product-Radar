package org.sni.spr.controller;

import org.sni.spr.store.Storer;

import java.time.*;
import java.util.List;
import java.util.concurrent.*;

public class Controller {

    private final ProductProvider provider;
    private final ProductService productService;
    private final Storer storer;

    public Controller(ProductProvider provider, ProductService productService, Storer storer) {
        this.provider = provider;
        this.productService = productService;
        this.storer = storer;
    }

    public void run(String sitemapUrl) {
        List<String> ids = provider.provideProductIDs(sitemapUrl);
        productService.getProducts(ids, storer::save);
    }

    public void scheduleDailyRun(String sitemapUrl, LocalTime executionTime) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                System.out.println("Starting daily scrape: " + LocalDateTime.now());
                run(sitemapUrl);
                System.out.println("Scrape finished: " + LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("Scheduled execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        };
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(executionTime.getHour())
                .withMinute(executionTime.getMinute())
                .withSecond(0);
        nextRun = !now.isBefore(nextRun) ? nextRun.plusDays(1) : nextRun;
        scheduler.scheduleAtFixedRate(
                task,
                Duration.between(now, nextRun).toSeconds(),
                Duration.ofDays(1).toSeconds(),
                TimeUnit.SECONDS
        );
    }
}