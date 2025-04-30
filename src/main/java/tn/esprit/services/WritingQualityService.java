// tn/esprit/services/WritingQualityService.java
package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WritingQualityService {
    private static final String API_URL = "https://api.languagetool.org/v2/check";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static class WritingIssue {
        private final String message;
        private final String category;
        private final int offset;
        private final int length;

        public WritingIssue(String message, String category, int offset, int length) {
            this.message = message;
            this.category = category;
            this.offset = offset;
            this.length = length;
        }

        // Getters
        public String getMessage() { return message; }
        public String getCategory() { return category; }
        public int getOffset() { return offset; }
        public int getLength() { return length; }
    }

    public List<WritingIssue> checkText(String text) throws IOException, InterruptedException {
        List<WritingIssue> issues = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "text=" + text + "&language=en-US"))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray matches = json.getJSONArray("matches");

        for (int i = 0; i < matches.length(); i++) {
            JSONObject match = matches.getJSONObject(i);
            JSONObject rule = match.getJSONObject("rule");

            // Corrected category access
            String category = rule.getJSONObject("category").getString("name");
            String message = match.getString("message");

            JSONObject context = match.getJSONObject("context");
            issues.add(new WritingIssue(
                    message,
                    category,
                    context.getInt("offset"),
                    context.getInt("length")
            ));
        }

        return issues;
    }
}