package com.bot.slack.cab.service.impl;

import com.bot.slack.cab.config.GeminiApiConfig;
import com.bot.slack.cab.model.ContentRequest;
import com.bot.slack.cab.service.GerminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class    GerminiServiceImplement implements GerminiService {

    @Autowired
    private GeminiApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String callGemini(String input) {
        try {
            // Tạo request body
            var part = new ContentRequest.Part(input);
            var content = new ContentRequest.Content(List.of(part));
            var body = new ContentRequest(List.of(content));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Endpoint with key
            String fullUrl = config.getApiUrl() + "?key=" + config.getApiKey();

            // Build request
            HttpEntity<ContentRequest> requestEntity = new HttpEntity<>(body, headers);

            // Gửi request
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return extractText(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String extractText(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
