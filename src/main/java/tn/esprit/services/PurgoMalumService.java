// tn/esprit/services/PurgoMalumService.java
package tn.esprit.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PurgoMalumService {
    private static final String API_URL = "https://www.purgomalum.com/service/";

    public boolean containsProfanity(String text) throws IOException {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        URL url = new URL(API_URL + "containsprofanity?text=" + encodedText);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return Boolean.parseBoolean(reader.readLine());
        }
    }

    public String censorText(String text) throws IOException {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        URL url = new URL(API_URL + "json?text=" + encodedText + "&fill=*");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString().replaceAll(".*\"result\":\"(.*?)\".*", "$1");
        }
    }
}