package com.hoxlabs.calorieai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.service.AiNutritionService;
import lombok.RequiredArgsConstructor;
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
public class PollinationsNutritionService implements AiNutritionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    // Pollinations.ai OpenAI-compatible endpoint
    private static final String POLLINATIONS_URL = "https://text.pollinations.ai/openai";
    // Using a default model supported by Pollinations
    private static final String MODEL = "openai"; 
    
    // Updated prompt to be more specific for Pollinations if needed, but keeping original structure
    private static final String PROMPT_TEMPLATE = "You are a nutrition expert. Analyze the following meal and provide nutritional content (Calories, Protein, Carbs, Fat) per item. Use Indian food standards if applicable. Return strict JSON format: {\"foodItems\": [{\"name\": \"...\", \"calories\": 0, \"protein\": 0.0, \"carbs\": 0.0, \"fat\": 0.0}]}. Do not include markdown formatting like ```json. Return ONLY JSON. Meal: ";

    @Override
    public AiNutritionResponse analyzeMeal(String mealText) throws JsonProcessingException {
        // Build request body per OpenAI Chat Completion format (supported by Pollinations)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", PROMPT_TEMPLATE + mealText);
        
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Pollinations doesn't technically require a key, but sending a dummy one is safer for compatibility
        headers.setBearerAuth("dummy-key");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(POLLINATIONS_URL, entity, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) messageObj.get("content");
                    
                    // Specific cleaning for Pollinations which might be chatty or add markdown
                    content = content.replace("```json", "").replace("```", "").trim();
                    
                    // Attempt to find the JSON object if there's extra text
                    int jsonStart = content.indexOf("{");
                    int jsonEnd = content.lastIndexOf("}");
                    if (jsonStart != -1 && jsonEnd != -1) {
                        content = content.substring(jsonStart, jsonEnd + 1);
                    }

                    return objectMapper.readValue(content, AiNutritionResponse.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Pollinations AI Call failed: " + e.getMessage());
            e.printStackTrace();
            return getMockResponse();
        }
        
        return getMockResponse();
    }

    private AiNutritionResponse getMockResponse() {
        AiNutritionResponse response = new AiNutritionResponse();
        AiNutritionResponse.FoodItemDto item1 = new AiNutritionResponse.FoodItemDto("Roti (Mock)", 120, 3.0, 20.0, 2.0);
        AiNutritionResponse.FoodItemDto item2 = new AiNutritionResponse.FoodItemDto("Paneer (Mock)", 250, 10.0, 15.0, 18.0);
        response.setFoodItems(List.of(item1, item2));
        return response;
    }
}
