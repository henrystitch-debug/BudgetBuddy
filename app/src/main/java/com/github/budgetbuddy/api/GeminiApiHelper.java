package com.github.budgetbuddy.api;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;

public class GeminiApiHelper {

    public interface ApiCallback {
        void onSuccess(String recommendation);
        void onError(String error);
    }

    private static final String MODEL_NAME = "gemini-3-flash-preview";

    public static void getBudgetRecommendation(
            String categoryName,
            String currency,
            String thisMonthSpend,
            String lastMonthSpend,
            String threeMonthAvg,
            ApiCallback callback) {

        String promptText = "I use a budget tracking app. " +
                "Category: " + categoryName + ". Currency: " + currency + ". " +
                "This month I spent " + currency + thisMonthSpend + ". " +
                "Last month I spent " + currency + lastMonthSpend +  ". " +
                "My 3-month average is " + currency + threeMonthAvg + ". " +
                "Recommend a monthly budget for this category. " +
                "Reply in 2-3 sentences and suggest a specific amount.";

        try {
            // Initialize using the production Firebase AI Logic configuration
            GenerativeModel firebaseAI = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel(MODEL_NAME);

            // Wrap the model to expose Java-compatible ListenableFuture calls
            GenerativeModelFutures model = GenerativeModelFutures.from(firebaseAI);

            // Constructing content via the verified type package builder
            Content contentPrompt = new Content.Builder()
                    .addText(promptText)
                    .build();

            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(contentPrompt);

            // Attach asynchronous callbacks to your custom thread executor pool
            Futures.addCallback(responseFuture, new FutureCallback<>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    if (result != null && result.getText() != null) {
                        callback.onSuccess(result.getText().trim());
                    } else {
                        callback.onError("Received empty response from Gemini.");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onError("Gemini API error: " + t.getLocalizedMessage());
                }
            }, com.github.budgetbuddy.database.AppDatabase.databaseWriteExecutor);

        } catch (Exception e) {
            callback.onError("Initialization error: " + e.getMessage());
        }
    }
}