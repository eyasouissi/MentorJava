package tn.esprit.services;

import tn.esprit.models.AICourseSlide;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeepInfraService {
    private static final String API_URL = "https://api.deepinfra.com/v1/openai/chat/completions";
    private static final String API_KEY = "wLtXOG0jAUL9fdICUVVx67GIS0C0Ii5F"; // Your key here

    public List<AICourseSlide> generateCourse(String prompt, String model) throws Exception {
        String requestBody = buildRequestBody(prompt, model);
        HttpRequest request = buildRequest(requestBody);

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error: " + response.statusCode() + " - " + response.body());
        }

        return parseResponse(response.body());
    }

    private HttpRequest buildRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private String buildRequestBody(String prompt, String model) {
        return new JSONObject()
                .put("model", model)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", buildPrompt(prompt))))
                .toString();
    }

    private String buildPrompt(String prompt) {
        return String.format("""
            Generate a course with 3-5 slides in JSON format.
            Each slide must have 'title' and 'content' fields.
            Content should be in markdown format with bullet points.
            
            Required JSON structure:
            {
              "slides": [
                {"title": "...", "content": "..."}
              ]
            }
            
            Topic: %s
            """, prompt);
    }

    private List<AICourseSlide> parseResponse(String json) {
        JSONObject response = new JSONObject(json);
        String content = response.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // Extract JSON from the response (may be wrapped in markdown)
        String jsonPart = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
        JSONObject data = new JSONObject(jsonPart);

        List<AICourseSlide> slides = new ArrayList<>();
        JSONArray jsonSlides = data.getJSONArray("slides");

        for (int i = 0; i < jsonSlides.length(); i++) {
            JSONObject slide = jsonSlides.getJSONObject(i);
            slides.add(new AICourseSlide(
                    slide.getString("title"),
                    slide.getString("content")
            ));
        }

        return slides;
    }

    // For testing without API calls
    public List<AICourseSlide> mockGenerateCourse(String prompt) {
        return List.of(
                new AICourseSlide("Introduction", "This is a mock response for:\n\n• " + prompt),
                new AICourseSlide("Main Content", "Key points:\n\n• Point 1\n• Point 2\n• Point 3"),
                new AICourseSlide("Summary", "This would be real AI content with DeepInfra")
        );
    }
}