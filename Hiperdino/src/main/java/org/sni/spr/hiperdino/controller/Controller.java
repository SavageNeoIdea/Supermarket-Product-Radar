package org.sni.spr.hiperdino.controller;

import org.sni.spr.hiperdino.controller.feeder.ProductFeeder;
import org.sni.spr.hiperdino.model.RawCategoryProductBatch;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.controller.store.Store;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Controller {
    private final ProductFeeder productFeeder;
    private final Store store;
    private final WebScraper webScraper;
    private final ExecutorService storeExecutor = Executors.newFixedThreadPool(4);
    private AtomicInteger productCounter;

    public Controller(ProductFeeder productFeeder, Store store, WebScraper webScraper) {
        this.productFeeder = productFeeder;
        this.store = store;
        this.webScraper = webScraper;
    }

    public void init() {
        long startTime = System.currentTimeMillis();
        resetProductCounter();
        webScraper.startScraping(getJsonConsumer());
        long endTime = System.currentTimeMillis();
        printSummary("Proceso Real", productCounter.get(), startTime, endTime);
    }

    private void resetProductCounter() {
        productCounter = new AtomicInteger(0);
    }

    private Consumer<HiperdinoProduct> getProductConsumer() {
        return product -> {
            storeWithThreads(product);
            productCounter.incrementAndGet();
        };
    }

    private Consumer<RawCategoryProductBatch> getJsonConsumer() {
        return this::processRawJson;
    }

    private void processRawJson(RawCategoryProductBatch scraperRawPayloads) {
        productFeeder.feed(scraperRawPayloads, getProductConsumer());
    }

    public void startScheduler(LocalTime executionTime) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long dailyTimeDelay = getDayDelay(executionTime);
        Runnable initRoutine = getInitializationRoutine();
        scheduler.scheduleAtFixedRate(initRoutine, dailyTimeDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private long getDayDelay(LocalTime executionTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = calculateNextRun(executionTime, now);
        return Duration.between(now, nextRun).toSeconds();
    }

    private Runnable getInitializationRoutine() {
        return () -> {
            try {
                init();
            } catch (Exception e) {
                System.err.println("Error en la ejecución programada: " + e.getMessage());
            }
        };
    }

    private LocalDateTime calculateNextRun(LocalTime executionTime, LocalDateTime now) {
        LocalDateTime nextRun = now.with(executionTime).withNano(0);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return nextRun;
    }

    private void storeWithThreads(HiperdinoProduct product) {
        storeExecutor.submit(() -> {
            try {
                store.storeSingleData(product);
            } catch (Exception e) {
                System.err.println("Error enviando producto " + product.getHiperdinoEventId() + ": " + e.getMessage());
            }
        });
    }

    private void printSummary(String type, int total, long start, long end) {
        double seconds = (end - start) / 1000.0;
        System.out.println("\n--- SUMMARY ---");
        System.out.println("Tipo: " + type);
        System.out.println("Productos procesados: " + total);
        System.out.printf("Tiempo total: %.2f segundos%n", seconds);
        System.out.println("----------------\n");
    }
}