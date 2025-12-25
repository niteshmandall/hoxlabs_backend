package com.hoxlabs.calorieai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PollinationsNutritionService service;

    private Map<String, Object> mockSuccessResponse;

    @BeforeEach
    void setUp() {
        // Construct a mock map that resembles OpenAI response structure
        mockSuccessResponse = new HashMap<>();
        Map<String, Object> choice = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        message.put("content", "{\"foodItems\": [{\"name\": \"Apple\", \"calories\": 95}]}");
        choice.put("message", message);
        mockSuccessResponse.put("choices", List.of(choice));
    }

    @Test
    void analyzeMeal_ShouldReturnData_WhenApiReturnsValidJson() throws JsonProcessingException {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockSuccessResponse);

        AiNutritionResponse expectedResponse = new AiNutritionResponse();
        when(objectMapper.readValue(anyString(), eq(AiNutritionResponse.class)))
                .thenReturn(expectedResponse);

        AiNutritionResponse result = service.analyzeMeal("One apple");

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void analyzeMeal_ShouldReturnFallback_WhenApiThrowsException() throws Exception {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("API Error"));

        AiNutritionResponse result = service.analyzeMeal("One apple");

        assertNotNull(result);
        assertFalse(result.getFoodItems().isEmpty());
        assertEquals("Roti (Mock)", result.getFoodItems().get(0).getName());
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
