package com.bot.slack.cab.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeminiApiConfigTest {

    @Autowired
    private GeminiApiConfig geminiApiConfig;

    @Test
    void testConfigProperties() {
        // Then
        assertNotNull(geminiApiConfig.getApiKey());
        assertNotNull(geminiApiConfig.getApiUrl());
        
        // Verify values from application.properties
        assertEquals("AIzaSyCWqnFnv2otNClRhFtLsStV7s8VuE9B_e8", geminiApiConfig.getApiKey());
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent", 
                geminiApiConfig.getApiUrl());
    }

    @Test
    void testRestTemplateBean() {
        // When
        RestTemplate restTemplate = geminiApiConfig.restTemplate();
        
        // Then
        assertNotNull(restTemplate);
    }
}
