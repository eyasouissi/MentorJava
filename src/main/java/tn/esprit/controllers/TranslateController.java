package tn.esprit.controllers;

import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TranslateController {
    private static final String[] ENDPOINTS = {
            "https://api.mymemory.translated.net/get?q={text}&langpair=en|{target}",
            "https://api-free.deepl.com/v2/translate?auth_key={key}&text={text}&target_lang={target}"
    };

    public static String translateText(String text, String targetLang) throws IOException {
        // Try MyMemory first (no API key needed)
        try {
            return tryMyMemory(text, targetLang);
        } catch (Exception e) {
            // Fallback to DeepL if configured
            return tryDeepL(text, targetLang);
        }
    }

    private static String tryMyMemory(String text, String target) throws IOException {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = ENDPOINTS[0]
                .replace("{text}", encodedText)
                .replace("{target}", target);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            JSONObject response = new JSONObject(br.readLine());
            return response.getJSONObject("responseData").getString("translatedText");
        }
    }

    private static String tryDeepL(String text, String target) throws IOException {
        String apiKey = "YOUR_DEEPL_FREE_KEY"; // Get free key: https://www.deepl.com/pro-api
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("DeepL API key not configured");
        }

        String url = ENDPOINTS[1]
                .replace("{key}", apiKey)
                .replace("{text}", URLEncoder.encode(text, StandardCharsets.UTF_8))
                .replace("{target}", target);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            JSONObject response = new JSONObject(br.readLine());
            return response.getJSONArray("translations")
                    .getJSONObject(0)
                    .getString("text");
        }
    }
}