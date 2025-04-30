package tn.esprit.controllers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class NotesAIController {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";

    public String summarizeText(String text) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("inputs", text);
        payload.put("parameters", new JSONObject()
                .put("max_length", 150)
                .put("min_length", 30)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("API Response: " + response.body()); // Debug log

        // Handle different response formats
        if (response.body().startsWith("[")) {
            JSONArray jsonArray = new JSONArray(response.body());
            return jsonArray.getJSONObject(0).getString("summary_text");
        } else if (response.body().startsWith("{")) {
            JSONObject json = new JSONObject(response.body());
            if (json.has("error")) {
                throw new Exception("Model loading: " + json.getString("error"));
            }
            return json.getString("summary_text");
        }
        throw new Exception("Unexpected response format");
    }
}