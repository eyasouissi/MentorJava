package tn.esprit.services;

import okhttp3.*;
import com.google.gson.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class AIImageService {
    private static final String DREAMSTUDIO_API_URL = "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image";
    private static final String API_KEY = "sk-n9aAiX67uZBzYxqxcHtphdB2yTz0FeVSRUFVGL9zfsbyLGxv"; // Replace with real API key

    public static File generateImage(String prompt) throws IOException {
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new IllegalArgumentException("Prompt must not be null or empty.");
            }

            OkHttpClient client = new OkHttpClient();

            // Create the request body with valid dimensions for SDXL
            JsonObject body = new JsonObject();
            JsonArray textPrompts = new JsonArray();
            JsonObject textPrompt = new JsonObject();
            textPrompt.addProperty("text", prompt);
            textPrompt.addProperty("weight", 1.0);
            textPrompts.add(textPrompt);
            body.add("text_prompts", textPrompts);
            body.addProperty("cfg_scale", 7);
            body.addProperty("width", 1024); // Using standard 1024x1024 dimensions
            body.addProperty("height", 1024); // for SDXL model
            body.addProperty("steps", 30);
            body.addProperty("samples", 1);
            body.addProperty("style_preset", "digital-art"); // Added style preset for better results

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    body.toString()
            );

            Request request = new Request.Builder()
                    .url(DREAMSTUDIO_API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            System.out.println("Sending request to API with body: " + body.toString());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    throw new IOException("API request failed: " + response.code() + " - " + errorBody);
                }

                // Parse the response
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                JsonArray artifacts = jsonResponse.getAsJsonArray("artifacts");
                if (artifacts == null || artifacts.size() == 0) {
                    throw new IOException("No images were generated in the response");
                }

                JsonObject firstImage = artifacts.get(0).getAsJsonObject();
                String base64Image = firstImage.get("base64").getAsString();
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Save the image to a temporary file
                File file = File.createTempFile("generated_image_", ".png");
                Files.write(file.toPath(), imageBytes);

                System.out.println("Image successfully generated and saved to: " + file.getAbsolutePath());
                return file;
            }
        } catch (Exception e) {
            System.err.println("Error during image generation: " + e.getMessage());
            throw new IOException("Failed to generate image: " + e.getMessage(), e);
        }
    }
}