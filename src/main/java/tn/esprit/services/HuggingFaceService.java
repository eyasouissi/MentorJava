package tn.esprit.services;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class HuggingFaceService {
    private static final String API_URL = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";
    private static final String API_TOKEN = "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"; // Replace with your token

    public double getSimilarityScore(String text1, String text2) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_URL);
            post.setHeader("Authorization", "Bearer " + API_TOKEN);
            post.setHeader("Content-Type", "application/json");

            JSONObject payload = new JSONObject()
                    .put("inputs", new JSONObject()
                            .put("source_sentence", text1)
                            .put("sentences", new JSONArray().put(text2))
                    )
                    .put("options", new JSONObject().put("wait_for_model", true)); // Moved outside

            post.setEntity(new StringEntity(payload.toString()));

            // Execute and parse response
            String response = EntityUtils.toString(client.execute(post).getEntity());
            JSONArray results = new JSONArray(response);
            return results.getDouble(0);
        }
    }

}