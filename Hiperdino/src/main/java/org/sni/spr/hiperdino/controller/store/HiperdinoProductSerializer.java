package org.sni.spr.hiperdino.controller.store;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.sni.spr.hiperdino.model.HiperdinoProduct;
import java.io.BufferedWriter;
import java.io.IOException;

public class HiperdinoProductSerializer implements Store {

    private static final String FILE_PATH = "products.json";

    @Override
    public void storeAllData(List<HiperdinoProduct> products) {
        // Configuramos GSON con el adaptador para LocalDateTime y formato bonito
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(products, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el archivo JSON: " + e.getMessage(), e);
        }
        System.out.println("Serialización de " + products.size() + " productos completada.");
    }
}
