// tn/esprit/services/GiphyService.java
package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GiphyService {
    private static final String API_KEY = "0pF87inqotOEEGmXCytsb6VS7NHebt6a";
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs";

    public List<String> searchGifs(String query) throws Exception {
        return fetchGifs("search?q=" + query + "&limit=10");
    }

    public List<String> getTrendingGifs() throws Exception {
        return fetchGifs("trending?limit=10");
    }

    private List<String> fetchGifs(String endpoint) throws Exception {
        List<String> gifUrls = new ArrayList<>();

        // Ensure the API key is appended correctly
        String fullUrl = endpoint.contains("?")
                ? BASE_URL + "/" + endpoint + "&api_key=" + API_KEY
                : BASE_URL + "/" + endpoint + "?api_key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch GIFs: HTTP " + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());

        if (!json.has("data")) {
            throw new RuntimeException("No 'data' array in response");
        }

        JSONArray gifs = json.getJSONArray("data");

        for (int i = 0; i < gifs.length(); i++) {
            JSONObject gif = gifs.getJSONObject(i);
            JSONObject images = gif.getJSONObject("images");

            if (images.has("fixed_width")) {
                JSONObject fixedWidth = images.getJSONObject("fixed_width");
                String url = fixedWidth.getString("url");
                gifUrls.add(url);
            }
        }

        return gifUrls;
    }
}
