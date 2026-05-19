package org.sni.spr.hiperdino.controller;

import org.sni.spr.hiperdino.controller.feeder.ProductFeeder;
import org.sni.spr.hiperdino.controller.simulationForTesting.HiperdinoSimulation;
import org.sni.spr.hiperdino.store.Store;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Controller {
    private final ProductFeeder productFeeder;
    private final Store store;
    private final ExecutorService storeExecutor = Executors.newFixedThreadPool(4);

    public Controller(ProductFeeder productFeeder, Store store) {
        this.productFeeder = productFeeder;
        this.store = store;
    }

    public void init() {
        long startTime = System.currentTimeMillis();
        AtomicInteger productCounter = new AtomicInteger(0);
        Consumer<HiperdinoProduct> productConsumer = getProductConsumer(productCounter);
        productFeeder.extractNewProducts(productConsumer);
        long endTime = System.currentTimeMillis();
        printSummary("Proceso Real", productCounter.get(), startTime, endTime);
    }

    private Consumer<HiperdinoProduct> getProductConsumer(AtomicInteger productCounter) {
        return product -> {
            storeWithThreads(product);
            productCounter.incrementAndGet();
        };
    }

    public void startScheduler(LocalTime executionTime) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long dailyTimeDelay = getDayDelay(executionTime);
        Runnable initRoutine = getInitializationRoutine();
        scheduler.scheduleAtFixedRate(initRoutine, dailyTimeDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private long getDayDelay(LocalTime executionTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = calculateNextRun(executionTime,now);
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

    private LocalDateTime calculateNextRun(LocalTime executionTime, LocalDateTime now){
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

    public void initSimulation() {
        System.out.println("Iniciando SIMULACIÓN de carga...");
        long startTime = System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger(0);
        store.storeAllData(new HiperdinoSimulation().init());
        long endTime = System.currentTimeMillis();
        printSummary("Simulación", counter.get(), startTime, endTime);
    }
}