package com.bot.slack.cab.service;

import com.bot.slack.cab.config.GeminiApiConfig;
import com.bot.slack.cab.model.ContentRequest;
import com.bot.slack.cab.service.impl.GerminiServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GerminiServiceTest {

    @Mock
    private GeminiApiConfig geminiApiConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GerminiServiceImplement germiniService;

    @Captor
    private ArgumentCaptor<HttpEntity<ContentRequest>> requestCaptor;

    private static final String API_KEY = "test-api-key";
    private static final String API_URL = "https://test-api-url.com";
    private static final String TEST_QUESTION = "What is the meaning of life?";
    private static final String TEST_RESPONSE = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"The meaning of life is 42\"}]}}]}";
    private static final String EXPECTED_ANSWER = "The meaning of life is 42";

    @BeforeEach
    void setUp() {
        // Only set up mocks for tests that need them
    }

    @Test
    void testCallGemini_Success() {
        // Given
        when(geminiApiConfig.getApiKey()).thenReturn(API_KEY);
        when(geminiApiConfig.getApiUrl()).thenReturn(API_URL);

        ResponseEntity<String> mockResponse = new ResponseEntity<>(TEST_RESPONSE, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // When
        String result = germiniService.callGemini(TEST_QUESTION);

        // Then
        assertEquals(EXPECTED_ANSWER, result);

        // Verify the request was made with correct parameters
        verify(restTemplate).exchange(
                eq(API_URL + "?key=" + API_KEY),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(String.class)
        );

        // Verify request body
        HttpEntity<ContentRequest> capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest);

        // Verify headers
        HttpHeaders headers = capturedRequest.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());

        // Verify content
        ContentRequest body = capturedRequest.getBody();
        assertNotNull(body);
        assertNotNull(body.contents);
        assertEquals(1, body.contents.size());
        assertNotNull(body.contents.get(0).parts);
        assertEquals(1, body.contents.get(0).parts.size());
        assertEquals(TEST_QUESTION, body.contents.get(0).parts.get(0).text);
    }

    @Test
    void testCallGemini_Exception() {
        // Given
        when(geminiApiConfig.getApiKey()).thenReturn(API_KEY);
        when(geminiApiConfig.getApiUrl()).thenReturn(API_URL);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("API Error"));

        // When
        String result = germiniService.callGemini(TEST_QUESTION);

        // Then
        assertNull(result);
    }

    // Extract text tests moved to GerminiServiceExtractTextTest
}
