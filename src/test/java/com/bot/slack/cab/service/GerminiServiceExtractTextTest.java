package com.bot.slack.cab.service;

import com.bot.slack.cab.service.impl.GerminiServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GerminiServiceExtractTextTest {

    private GerminiServiceImplement germiniService;
    
    private static final String TEST_RESPONSE = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"The meaning of life is 42\"}]}}]}";
    private static final String EXPECTED_ANSWER = "The meaning of life is 42";

    @BeforeEach
    void setUp() {
        germiniService = new GerminiServiceImplement();
    }

    @Test
    void testExtractText_ValidJson() {
        // When
        String result = germiniService.extractText(TEST_RESPONSE);
        
        // Then
        assertEquals(EXPECTED_ANSWER, result);
    }

    @Test
    void testExtractText_InvalidJson() {
        // When
        String result = germiniService.extractText("invalid json");
        
        // Then
        assertTrue(result.startsWith("Error parsing response:"));
    }

    @Test
    void testExtractText_MissingFields() {
        // Given
        String incompleteJson = "{\"candidates\":[{}]}";
        
        // When
        String result = germiniService.extractText(incompleteJson);
        
        // Then
        assertTrue(result.startsWith("Error parsing response:"));
    }
}
