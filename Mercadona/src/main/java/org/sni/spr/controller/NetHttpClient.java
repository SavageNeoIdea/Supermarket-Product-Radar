package org.sni.spr.controller;


import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

public class NetHttpClient implements HttpClient{
    private String response;

    @Override
    public String makeRequest(String url) {
        try {
            var conn = (HttpURLConnection) new URI(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                response = reader.lines().collect(Collectors.joining());
                return response;
            }
        } catch (Exception e) {
            throw new RuntimeException("HTTP error: " + url, e);
        }
    }

    public String getResponse() {
        return response;
    }

}
