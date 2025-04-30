package tn.esprit.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatGPTService {
    private static final String API_KEY = "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"; // Free tier still needs API key
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public static String getStudyBuddyResponse(String userMessage) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "mistralai/mistral-7b-instruct"); // Free model

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        requestBody.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("HTTP-Referer", "https://your-app-name.com") // Required
                .header("X-Title", "StudyBuddy (Free Tier)") // Recommended
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        // Debug output
        System.out.println("Free Model Response: " + response.body());

        JSONObject jsonResponse = new JSONObject(response.body());

        if (jsonResponse.has("error")) {
            throw new Exception("Error: " + jsonResponse.getJSONObject("error").getString("message"));
        }

        return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }
}