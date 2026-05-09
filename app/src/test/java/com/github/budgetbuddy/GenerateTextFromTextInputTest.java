package com.github.budgetbuddy;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.junit.Test;

public class GenerateTextFromTextInputTest {
    @Test
    public void generateText() {
        // The client gets the API key from the environment variable `GEMINI_API_KEY`.
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3-flash-preview",
                        "How do I track my expense and save more?",
                        null);

        System.out.println(response.text());
    }
}
