package com.github.budgetbuddy.api;

import com.github.budgetbuddy.database.AppDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClaudeApiHelper {

    public interface ApiCallback {
        void onSuccess(String recommendation);
        void onError(String error);
    }

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    public static void getBudgetRecommendation(
            String apiKey,
            String categoryName,
            String currency,
            String thisMonthSpend,
            String lastMonthSpend,
            String threeMonthAvg,
            ApiCallback callback) {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("No API key found.\nAdd ANTHROPIC_API_KEY=your_key to local.properties and rebuild.");
            return;
        }

        String prompt = "I use a budget tracking app. " +
                "Category: " + categoryName + ". Currency: " + currency + ". " +
                "This month I spent " + currency + thisMonthSpend + ". " +
                "Last month I spent " + currency + lastMonthSpend +  ". " +
                "My 3-month average is " + currency + threeMonthAvg + ". " +
                "Recommend a monthly budget for this category. " +
                "Reply in 2-3 sentences and suggest a specific amount.";

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject body = new JSONObject();
                body.put("model", MODEL);
                body.put("max_tokens", 200);
                body.put("messages", messages);

                RequestBody requestBody = RequestBody.create(
                        body.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .addHeader("content-type", "application/json")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        String text = json.getJSONArray("content")
                                .getJSONObject(0)
                                .getString("text");
                        callback.onSuccess(text);
                    } else {
                        callback.onError("API error " + response.code() + ": check your API key.");
                    }
                }
            } catch (Exception e) {
                callback.onError("Connection error: " + e.getMessage());
            }
        });
    }
}
