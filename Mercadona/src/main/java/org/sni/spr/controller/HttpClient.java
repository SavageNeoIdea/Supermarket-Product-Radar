package org.sni.spr.controller;


import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

public class HttpClient {

    public String makeRequest(String url) {
        try {
            var conn = (HttpURLConnection) new URI(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                return reader.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw new RuntimeException("HTTP error: " + url, e);
        }
    }
}
