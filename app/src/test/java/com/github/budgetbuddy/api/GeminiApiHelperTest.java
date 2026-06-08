package com.github.budgetbuddy.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GeminiApiHelperTest {

    private MockedStatic<FirebaseAI> mockedFirebaseAI;
    private MockedStatic<GenerativeModelFutures> mockedGenerativeModelFutures;
    private GenerativeModel mockGenerativeModel;
    private GenerativeModelFutures mockGenerativeModelFutures;
    private FirebaseAI mockFirebaseAIInstance;

    @Before
    public void setUp() {
        mockedFirebaseAI = mockStatic(FirebaseAI.class);
        mockedGenerativeModelFutures = mockStatic(GenerativeModelFutures.class);

        mockFirebaseAIInstance = mock(FirebaseAI.class);
        mockGenerativeModel = mock(GenerativeModel.class);
        mockGenerativeModelFutures = mock(GenerativeModelFutures.class);

        // Aligned to match the target initialization logic: FirebaseAI.getInstance(GenerativeBackend.googleAI())
        mockedFirebaseAI.when(() -> FirebaseAI.getInstance(any(GenerativeBackend.class)))
                .thenReturn(mockFirebaseAIInstance);

        when(mockFirebaseAIInstance.generativeModel(anyString()))
                .thenReturn(mockGenerativeModel);

        mockedGenerativeModelFutures.when(() ->
                        GenerativeModelFutures.from(any(GenerativeModel.class)))
                .thenReturn(mockGenerativeModelFutures);
    }

    @After
    public void tearDown() {
        mockedFirebaseAI.close();
        mockedGenerativeModelFutures.close();
    }

    @Test
    public void getBudgetRecommendation_success() throws InterruptedException {
        String expectedRecommendation = "Recommended budget is $500.";
        SettableFuture<GenerateContentResponse> future = SettableFuture.create();
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.getText()).thenReturn(expectedRecommendation);

        when(mockGenerativeModelFutures.generateContent(any(Content.class))).thenReturn(future);

        CountDownLatch latch = new CountDownLatch(1);
        GeminiApiHelper.ApiCallback callback = mock(GeminiApiHelper.ApiCallback.class);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(callback).onSuccess(anyString());

        // 1. Call the helper method to register the listener on your real background executor
        GeminiApiHelper.getBudgetRecommendation("Food", "USD",
                "100", "120", "110", callback);

        // 2. Satisfy the future, which automatically runs the success block on the background thread
        future.set(mockResponse);

        // 3. Wait for the background thread pool to process and count down
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Test timed out waiting for background executor");
        }
        verify(callback).onSuccess(expectedRecommendation);
    }

    @Test
    public void getBudgetRecommendation_error() throws InterruptedException {
        String errorMessage = "Network Error";
        SettableFuture<GenerateContentResponse> future = SettableFuture.create();

        when(mockGenerativeModelFutures.generateContent(any(Content.class))).thenReturn(future);

        CountDownLatch latch = new CountDownLatch(1);
        GeminiApiHelper.ApiCallback callback = mock(GeminiApiHelper.ApiCallback.class);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(callback).onError(anyString());

        GeminiApiHelper.getBudgetRecommendation("Food", "USD",
                "100", "120", "110", callback);

        // Simulating structural network/API exceptions
        future.setException(new RuntimeException(errorMessage));

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Test timed out waiting for background executor");
        }
        verify(callback).onError("Gemini API error: " + errorMessage);
    }

    @Test
    public void getBudgetRecommendation_emptyResponse() throws InterruptedException {
        SettableFuture<GenerateContentResponse> future = SettableFuture.create();
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.getText()).thenReturn(null);

        when(mockGenerativeModelFutures.generateContent(any(Content.class))).thenReturn(future);

        CountDownLatch latch = new CountDownLatch(1);
        GeminiApiHelper.ApiCallback callback = mock(GeminiApiHelper.ApiCallback.class);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(callback).onError(anyString());

        GeminiApiHelper.getBudgetRecommendation("Food", "USD",
                "100", "120", "110", callback);

        future.set(mockResponse);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Test timed out waiting for background executor");
        }
        verify(callback).onError("Received empty response from Gemini.");
    }

    @Test
    public void getBudgetRecommendation_initializationError() {
        mockedFirebaseAI.when(() -> FirebaseAI.getInstance(any(GenerativeBackend.class)))
                .thenThrow(new RuntimeException("Init failed"));

        GeminiApiHelper.ApiCallback callback = mock(GeminiApiHelper.ApiCallback.class);
        GeminiApiHelper.getBudgetRecommendation("Food", "USD",
                "100", "120", "110", callback);

        verify(callback).onError("Initialization error: Init failed");
    }
}