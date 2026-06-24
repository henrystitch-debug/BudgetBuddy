package com.github.budgetbuddy.api;

import android.os.Handler;
import android.os.Looper;

import com.github.budgetbuddy.BuildConfig;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class to generate budget recommendations using the Gemini API.
 */
public class GenerateTextFromInput {

    public interface ApiCallback {
        void onSuccess(String recommendation);
        void onError(String error);
    }

    private static final String MODEL_NAME = "gemini-2.0-flash";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Fetches a budget recommendation from Gemini based on spending history.
     */
    public static void getBudgetRecommendation(
            String categoryName,
            String currency,
            String thisMonthSpend,
            String lastMonthSpend,
            String threeMonthAvg,
            ApiCallback callback) {

        String geminiApiKey = BuildConfig.GOOGLE_API_KEY;

        String promptText = "I use a budget tracking app. " +
                "Category: " + categoryName + ". Currency: " + currency + ". " +
                "This month I spent " + currency + thisMonthSpend + ". " +
                "Last month I spent " + currency + lastMonthSpend + ". " +
                "My 3-month average is " + currency + threeMonthAvg + ". " +
                "Recommend a monthly budget for this category. " +
                "Reply in 2-3 sentences and suggest a specific amount.";

        executor.execute(() -> {
            try (Client client = Client.builder()
                    .apiKey(geminiApiKey)
                    .build()) {

                GenerateContentResponse response = client.models.generateContent(
                        MODEL_NAME,
                        promptText,
                        GenerateContentConfig.builder().build()
                );

                String result = response.text();
                mainHandler.post(() -> callback.onSuccess(result));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
