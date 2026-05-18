package org.sni.spr.hiperdino.controller;

import org.sni.spr.hiperdino.controller.feeder.ProductFeeder;
import org.sni.spr.hiperdino.controller.simulationForTesting.HiperdinoSimulation;
import org.sni.spr.hiperdino.controller.store.Store;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import javax.jms.JMSException;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    private final ProductFeeder productFeeder;
    private final Store store;
    private final ExecutorService storeExecutor = Executors.newFixedThreadPool(4);

    public Controller(ProductFeeder productFeeder, Store store) {
        this.productFeeder = productFeeder;
        this.store = store;
    }

    public void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Atlantic/Canary"));
        ZonedDateTime nextRun = now.withHour(19).withMinute(0).withSecond(0).withNano(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toSeconds();

        System.out.println("📅 Scheduler iniciado. Próxima ejecución: " + nextRun);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                init();
            } catch (Exception e) {
                System.err.println("Error en la ejecución programada: " + e.getMessage());
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void init() {
        System.out.println("Iniciando extracción y envío en tiempo real...");
        long startTime = System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger(0);
        productFeeder.extractAndStream(product -> {
            processAndStore(product);
            counter.incrementAndGet();
        });
        long endTime = System.currentTimeMillis();
        printSummary("Proceso Real", counter.get(), startTime, endTime);
    }

    public void initSimulation() {
        System.out.println("Iniciando SIMULACIÓN de carga...");
        long startTime = System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger(0);
        HiperdinoSimulation simulation = new HiperdinoSimulation();
        List<HiperdinoProduct> productList = simulation.init();
        store.storeAllData(productList);
        long endTime = System.currentTimeMillis();
        printSummary("Simulación", counter.get(), startTime, endTime);
    }

    private void processAndStore(HiperdinoProduct product) {
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