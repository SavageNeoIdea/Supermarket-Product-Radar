package org.sni.spr.hiperdino.controller;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.*;

import org.sni.spr.hiperdino.controller.simulationForTesting.HiperdinoSimulation;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.controller.feeder.ProductFeeder;
import org.sni.spr.hiperdino.controller.store.Store;

import javax.jms.JMSException;
import java.util.List;

public class Controller {
    private final ProductFeeder productFeeder;
    private final Store store;

    public Controller(ProductFeeder productFeeder, Store store) {
        this.productFeeder = productFeeder;
        this.store = store;
    }

    public void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Atlantic/Canary"));
        ZonedDateTime nextRun = now.withHour(19).withMinute(0).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }
        long initialDelay = Duration.between(now, nextRun).toSeconds();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void init() throws JMSException {
        long startTime = System.currentTimeMillis();
        List<HiperdinoProduct> products = productFeeder.extractAndTransformProducts();
        store.storeAllData(products);
        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double seconds = durationMillis / 1000.0;
        long minutes = (long) seconds / 60;
        double remainingSeconds = seconds % 60;
        System.out.println("---");
        System.out.println("Proceso finalizado con éxito.");
        System.out.printf("Duración total: %d min y %.2f seg (Total: %.2f seg)%n",
                minutes, remainingSeconds, seconds);
        System.out.println("---");
    }


    public void initSimulation() throws JMSException {
        System.out.println("=== INICIANDO SIMULACIÓN DE CARGA (ActiveMQ) ===");
        long startTime = System.currentTimeMillis();
        HiperdinoSimulation simulation = new HiperdinoSimulation();
        List<HiperdinoProduct> mockProducts = simulation.init();
        int totalProducts = mockProducts.size();
        System.out.println("Simulador: Generados " + totalProducts + " productos de prueba.");
        System.out.println("Enviando datos al broker de mensajería...");
        store.storeAllData(mockProducts);
        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;
        System.out.println("---");
        System.out.println("Simulación finalizada con éxito.");
        System.out.printf("Se han procesado y enviado %d mensajes a ActiveMQ.%n", totalProducts);
        System.out.printf("Tiempo de ejecución de simulación: %.3f segundos.%n", durationSeconds);
        System.out.println("---");
    }
}