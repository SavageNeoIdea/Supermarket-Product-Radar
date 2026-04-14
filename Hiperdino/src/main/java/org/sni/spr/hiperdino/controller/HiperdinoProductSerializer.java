package org.sni.spr.hiperdino.controller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import org.sni.spr.hiperdino.model.Product;

public class HiperdinoProductSerializer implements Storer{

    private static final String FILE_PATH = "products.json";

    @Override
    public void storeAllData(Map<String, List<Product>> products) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .setPrettyPrinting()
                .create();
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(FILE_PATH), StandardCharsets.UTF_8);
             JsonWriter jsonWriter = new JsonWriter(writer)) {

            jsonWriter.beginObject();
            jsonWriter.setIndent("\t");

            for (Map.Entry<String, List<Product>> entry : products.entrySet()) {
                String categorySubcategory = entry.getKey();
                List<Product> productList = entry.getValue();

                jsonWriter.name(categorySubcategory);
                jsonWriter.beginArray();
                for (Product product : productList) {
                    gson.toJson(product, Product.class, jsonWriter);
                }
                jsonWriter.endArray();
            }
            jsonWriter.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Serialización de lista larga completada.");
    }
}
