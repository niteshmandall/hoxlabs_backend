package com.hoxlabs.calorieai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollinationsNutritionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper(); // Use real ObjectMapper

    @InjectMocks
    private PollinationsNutritionService service;

    private Map<String, Object> mockSuccessResponse;

    @BeforeEach
    void setUp() {
        mockSuccessResponse = new HashMap<>();
        Map<String, Object> choice = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        
        // Complex JSON matching new schema
        String jsonContent = "{" +
                "\"items\": [" +
                "  {\"name\": \"Roti\", \"quantity\": \"2 pieces\", \"calories\": 240, \"protein\": 6.0, \"carbs\": 40.0, \"fat\": 4.0}," +
                "  {\"name\": \"Dal\", \"quantity\": \"1 bowl\", \"calories\": 150, \"protein\": 8.0, \"carbs\": 20.0, \"fat\": 5.0}" +
                "]," +
                "\"totals\": {\"calories\": 390, \"protein\": 14.0, \"carbs\": 60.0, \"fat\": 9.0}," +
                "\"confidence\": \"high\"" +
                "}";
        
        message.put("content", jsonContent);
        choice.put("message", message);
        mockSuccessResponse.put("choices", List.of(choice));
    }

    @Test
    void analyzeMeal_ShouldParseComplexJsonCorrectly() throws JsonProcessingException {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockSuccessResponse);

        AiNutritionResponse result = service.analyzeMeal("2 rotis and dal");

        assertNotNull(result);
        assertEquals(2, result.getFoodItems().size());
        
        // Verify Item 1
        assertEquals("Roti", result.getFoodItems().get(0).getName());
        assertEquals("2 pieces", result.getFoodItems().get(0).getQuantity());
        assertEquals(240, result.getFoodItems().get(0).getCalories());
        
        // Verify Item 2
        assertEquals("Dal", result.getFoodItems().get(1).getName());
        
        // Verify Totals
        assertNotNull(result.getTotals());
        assertEquals(390, result.getTotals().getCalories());
        assertEquals(14.0, result.getTotals().getProtein());
        
        // Verify Confidence
        assertEquals("high", result.getConfidence());
    }

    @Test
    void analyzeMeal_ShouldReturnFallback_WhenApiThrowsException() throws Exception {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("API Error"));

        AiNutritionResponse result = service.analyzeMeal("One apple");

        assertNotNull(result);
        assertFalse(result.getFoodItems().isEmpty());
        // Verify mock fallback data matches updated structure
        assertEquals("Roti (Mock)", result.getFoodItems().get(0).getName());
        assertEquals("low", result.getConfidence());
    }

    @Test
    void analyzeMeal_ShouldReturnFallback_WhenResponseIsEmpty() throws Exception {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new HashMap<>()); // Empty map

        AiNutritionResponse result = service.analyzeMeal("One apple");

        assertNotNull(result);
        assertEquals("Roti (Mock)", result.getFoodItems().get(0).getName());
    }
}
