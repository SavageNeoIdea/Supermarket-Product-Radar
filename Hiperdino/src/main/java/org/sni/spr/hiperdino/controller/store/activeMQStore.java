package org.sni.spr.hiperdino.controller.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activeMQStore implements Store{

    private static final String FILE_PATH = "products.json";

    @Override
    public void storeAllData(List<HiperdinoProduct> productList) {
         toEventJson(productList);

         /*
        ¿Enviar a active MQ de alguna manera?
         Duda: ¿Tengo que enviar los eventos de uno en uno al ActiveMQ, supongo que una
         lista de jsons por streams hacia el activeMQ...
         o tengo que enviarlos por lotes como el ejemplo comentado abajo del todo?
        */

    }

    public void toEventJson(List<HiperdinoProduct> products) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .disableHtmlEscaping()
                // .setPrettyPrinting()
                .create();

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(FILE_PATH), StandardCharsets.UTF_8)) {
            for (HiperdinoProduct product : products) {
                Map<String, Object> event = new HashMap<>();
                event.put("ts", LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                event.put("ss", "feeder-hiperdino");
                event.put("payload", product);

                writer.write(gson.toJson(event));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir los eventos: " + e.getMessage(), e);
        }
    }
}

/*
Output Example:
{"ts":"2026-04-21T12:00:00Z","ss":"feeder-hiperdino","payload":{"sku":"123","name":"Aceite",...}}
{"ts":"2026-04-21T12:00:01Z","ss":"feeder-hiperdino","payload":{"sku":"124","name":"Leche",...}}
*/
