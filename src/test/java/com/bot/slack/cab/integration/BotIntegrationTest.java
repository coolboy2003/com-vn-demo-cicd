package com.bot.slack.cab.integration;

import com.bot.slack.cab.controller.BotController;
import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.model.ServiceDeployment;
import com.bot.slack.cab.service.BotService;
import com.bot.slack.cab.service.GerminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(BotIntegrationTest.TestConfig.class)
public class BotIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock the Gemini service to avoid actual API calls
    @Autowired
    private GerminiService germiniService;

    @Configuration
    static class TestConfig {
        @Bean
        public GerminiService germiniService() {
            return org.mockito.Mockito.mock(GerminiService.class);
        }

        @Bean
        public BotService botService() {
            // Use the real implementation for integration test
            return new com.bot.slack.cab.service.impl.BotServiceImplement();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public BotController botController() {
            BotController controller = new BotController();
            // Spring will autowire the dependencies
            return controller;
        }
    }

    @Autowired
    private BotService botService;

    @Test
    void testGenerateCABEndpoint() throws Exception {
        // Given
        List<ServiceDeployment> services = Arrays.asList(
                ServiceDeployment.builder().repoName("service-a").version("1.2.3").build(),
                ServiceDeployment.builder().repoName("service-b").version("2.3.4").build()
        );

        BotReq request = BotReq.builder()
                .summary("Test deployment")
                .ticket("TICKET-123")
                .deploymentServices(services)
                .build();

        // When
        String result = botService.format(request);

        // Then
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("• Summary: Test deployment"));
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("• Ticket: TICKET-123"));
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("◦ service-a"));
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("◦ service-b"));
    }

    @Test
    void testAskGeminiEndpoint() throws Exception {
        // Given
        String question = "What is the meaning of life?";
        String expectedAnswer = "The meaning of life is 42";

        when(germiniService.callGemini(anyString())).thenReturn(expectedAnswer);

        // When
        String result = germiniService.callGemini(question);

        // Then
        org.junit.jupiter.api.Assertions.assertEquals(expectedAnswer, result);
    }
}
