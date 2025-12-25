package com.hoxlabs.calorieai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.service.AiNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiNutritionService implements AiNutritionService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String PROMPT_TEMPLATE = "You are a nutrition expert. Convert the following meal into calories, protein, carbs, and fat. Use Indian food standards. Return JSON in the following format: {\"foodItems\": [{\"name\": \"...\", \"calories\": 0, \"protein\": 0.0, \"carbs\": 0.0, \"fat\": 0.0}]}. Return ONLY JSON. Meal: ";

    @Override
    public AiNutritionResponse analyzeMeal(String mealText) throws JsonProcessingException {
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", PROMPT_TEMPLATE + mealText);
        
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // For a real app, handle exceptions properly
        try {
            Map<String, Object> response = restTemplate.postForObject(OPENAI_URL, entity, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) messageObj.get("content");
                    
                    // Simple cleaning of markdown code blocks if present
                    content = content.replace("```json", "").replace("```", "").trim();
                    
                    return objectMapper.readValue(content, AiNutritionResponse.class);
                }
            }
        } catch (Exception e) {
            // Fallback for demo purposes if API fails or key is missing
            System.err.println("AI Call failed: " + e.getMessage());
            return getMockResponse();
        }
        
        return getMockResponse();
    }

    private AiNutritionResponse getMockResponse() {
        AiNutritionResponse response = new AiNutritionResponse();
        AiNutritionResponse.FoodItemDto item1 = new AiNutritionResponse.FoodItemDto("Roti", 120, 3.0, 20.0, 2.0);
        AiNutritionResponse.FoodItemDto item2 = new AiNutritionResponse.FoodItemDto("Paneer Sabzi", 250, 10.0, 15.0, 18.0);
        response.setFoodItems(List.of(item1, item2));
        return response;
    }
}
