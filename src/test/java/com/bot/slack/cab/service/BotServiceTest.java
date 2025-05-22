package com.bot.slack.cab.service;

import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.model.ServiceDeployment;
import com.bot.slack.cab.service.impl.BotServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BotServiceTest {

    @InjectMocks
    private BotServiceImplement botService;

    private BotReq sampleRequest;

    @BeforeEach
    void setUp() {
        // Create a sample BotReq for testing
        List<ServiceDeployment> services = Arrays.asList(
                ServiceDeployment.builder().repoName("service-a").version("1.2.3").build(),
                ServiceDeployment.builder().repoName("service-b").version("2.3.4").build()
        );

        sampleRequest = BotReq.builder()
                .summary("Test deployment")
                .ticket("TICKET-123")
                .deploymentServices(services)
                .build();
    }

    @Test
    void testFormatWithValidRequest() {
        // When
        String result = botService.format(sampleRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("• Summary: Test deployment"));
        assertTrue(result.contains("• Ticket: TICKET-123"));
        assertTrue(result.contains("• Affected Services:"));
        assertTrue(result.contains("◦ service-a"));
        assertTrue(result.contains("◦ service-b"));
        assertTrue(result.contains("• Implementation Plan:"));
        assertTrue(result.contains("◦ Deploy the service-a version 1.2.3"));
        assertTrue(result.contains("◦ Deploy the service-b version 2.3.4"));
        assertTrue(result.contains("• Backout Plan:"));
        assertTrue(result.contains("◦ Rollback the service-a version 1.2.2"));
        assertTrue(result.contains("◦ Rollback the service-b version 2.3.3"));
    }

    @Test
    void testFormatWithNullRequest() {
        // When
        String result = botService.format(null);

        // Then
        assertEquals("Error: Request object is null.", result);
    }

    @Test
    void testFormatWithEmptyServices() {
        // Given
        BotReq emptyServicesReq = BotReq.builder()
                .summary("Test deployment")
                .ticket("TICKET-123")
                .deploymentServices(new ArrayList<>())
                .build();

        // When
        String result = botService.format(emptyServicesReq);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("• Summary: Test deployment"));
        assertTrue(result.contains("• Ticket: TICKET-123"));
        assertTrue(result.contains("• Affected Services: N/A"));
        assertTrue(result.contains("• Implementation Plan: N/A"));
        assertTrue(result.contains("• Backout Plan: N/A"));
    }

    @Test
    void testFormatWithNullServices() {
        // Given
        BotReq nullServicesReq = BotReq.builder()
                .summary("Test deployment")
                .ticket("TICKET-123")
                .deploymentServices(null)
                .build();

        // When
        String result = botService.format(nullServicesReq);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("• Summary: Test deployment"));
        assertTrue(result.contains("• Ticket: TICKET-123"));
        assertTrue(result.contains("• Affected Services: N/A"));
        assertTrue(result.contains("• Implementation Plan: N/A"));
        assertTrue(result.contains("• Backout Plan: N/A"));
    }

    @Test
    void testCalculatePreviousVersion() {
        // Test normal version calculation
        assertEquals("1.2.2", botService.calculatePreviousVersion("1.2.3"));
        assertEquals("2.3.0", botService.calculatePreviousVersion("2.3.1"));
        assertEquals("1.9.9", botService.calculatePreviousVersion("2.0.0"));
        
        // Test with zeros
        assertEquals("1.2.9", botService.calculatePreviousVersion("1.3.0"));
        
        // Test with null or empty
        assertNull(botService.calculatePreviousVersion(null));
        assertNull(botService.calculatePreviousVersion(""));
        assertNull(botService.calculatePreviousVersion("  "));
        
        // Test with invalid format
        assertNull(botService.calculatePreviousVersion("invalid"));
        
        // Test with all zeros
        assertNull(botService.calculatePreviousVersion("0.0.0"));
    }

    @Test
    void testIsValidServiceForPlan() {
        // Valid service
        ServiceDeployment validService = ServiceDeployment.builder()
                .repoName("service-a")
                .version("1.0.0")
                .build();
        assertTrue(botService.isValidServiceForPlan(validService));
        
        // Service with empty repo name
        ServiceDeployment emptyRepoService = ServiceDeployment.builder()
                .repoName("")
                .version("1.0.0")
                .build();
        assertFalse(botService.isValidServiceForPlan(emptyRepoService));
        
        // Service with null repo name
        ServiceDeployment nullRepoService = ServiceDeployment.builder()
                .repoName(null)
                .version("1.0.0")
                .build();
        assertFalse(botService.isValidServiceForPlan(nullRepoService));
        
        // Null service
        assertFalse(botService.isValidServiceForPlan(null));
    }

    @Test
    void testGetValueOrNA() {
        assertEquals("test", botService.getValueOrNA("test"));
        assertEquals("N/A", botService.getValueOrNA(null));
        assertEquals("N/A", botService.getValueOrNA(""));
        assertEquals("N/A", botService.getValueOrNA("  "));
    }
}
