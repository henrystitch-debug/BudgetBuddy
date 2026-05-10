package com.github.budgetbuddy;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class GenerateTextFromTextInputTest {
    @Test
    public void generateText() {
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        String runIntegration = System.getenv("RUN_GEMINI_INTEGRATION_TEST");
        Assume.assumeTrue(
                "Set GEMINI_API_KEY and RUN_GEMINI_INTEGRATION_TEST=true to run this integration test.",
                geminiApiKey != null && !geminiApiKey.isBlank() && "true".equalsIgnoreCase(runIntegration)
        );

        // The client gets the API key from the environment.
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3-flash-preview",
                        "How do I track my expense and save more?",
                        null);

        Assert.assertNotNull("Response must not be null", response);
        Assert.assertNotNull("Response text must not be null", response.text());
        Assert.assertFalse("Response text must not be blank", response.text().isBlank());
    }
}
