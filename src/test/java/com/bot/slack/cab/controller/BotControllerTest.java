package com.bot.slack.cab.controller;

import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.model.ServiceDeployment;
import com.bot.slack.cab.service.BotService;
import com.bot.slack.cab.service.GerminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BotController.class)
@Import(BotControllerTest.TestConfig.class)
public class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BotService botService;

    @Autowired
    private GerminiService germiniService;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    static class TestConfig {
        @Bean
        public BotService botService() {
            return org.mockito.Mockito.mock(BotService.class);
        }

        @Bean
        public GerminiService germiniService() {
            return org.mockito.Mockito.mock(GerminiService.class);
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

    @Test
    void testGenerateCAB_Success() throws Exception {
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

        String expectedResponse = "• Summary: Test deployment\n• Ticket: TICKET-123";
        when(botService.format(any(BotReq.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/bot/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testAskGemini_Success() throws Exception {
        // Given
        String question = "What is the meaning of life?";
        String expectedAnswer = "The meaning of life is 42";

        when(germiniService.callGemini(anyString())).thenReturn(expectedAnswer);

        // When & Then
        mockMvc.perform(get("/bot/ask")
                .param("question", question))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedAnswer));
    }

    @Test
    void testAskGemini_NullResponse() throws Exception {
        // Given
        String question = "What is the meaning of life?";

        when(germiniService.callGemini(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/bot/ask")
                .param("question", question))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
