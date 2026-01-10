package com.hoxlabs.calorieai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.service.AiNutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollinationsNutritionService implements AiNutritionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Pollinations.ai OpenAI-compatible endpoint
    private static final String POLLINATIONS_URL = "https://gen.pollinations.ai/v1/chat/completions";
    private static final String MODEL = "openai-fast"; 

    // 1. System Prompt (Set Once)
    private static final String SYSTEM_PROMPT = 
            "You are a certified nutritionist specializing in Indian food. " +
            "You convert meals into nutrition values using Indian cooking standards. " +
            "Always estimate realistic portion sizes. " +
            "Always return valid JSON. " +
            "Never include explanations. " +
            "Never include text outside JSON.";

    // 2. User Prompt Template
    private static final String USER_PROMPT_TEMPLATE = 
            "Convert the following meal into nutrition values.\n" +
            "Rules:\n" +
            "- Use Indian household portion sizes\n" +
            "- Estimate oil usage conservatively\n" +
            "- If portion is missing, assume average\n" +
            "- For vague inputs (e.g., 'I ate outside'), return empty items and ask for clarification\n" +
            "- For fast food without brand, use average portions\n" +
            "- For homemade Indian dishes (e.g. 'sabzi', 'dal'), assume 1 cup servings if unspecified\n" +
            "- Calories must be realistic\n" +
            "- Return macros per item and totals\n\n" +
            "Meal:\n\"%s\"\n\n" +
            "Return JSON only matching this schema:\n" +
            "{\n" +
            "  \"items\": [\n" +
            "    {\"name\": \"string\", \"quantity\": \"string\", \"calories\": int, \"protein\": double, \"carbs\": double, \"fat\": double}\n" +
            "  ],\n" +
            "  \"totals\": {\"calories\": int, \"protein\": double, \"carbs\": double, \"fat\": double},\n" +
            "  \"confidence\": \"high|medium|low\",\n" +
            "  \"clarification\": \"string (optional)\"\n" +
            "  \"confidence\": \"high|medium|low\",\n" +
            "  \"clarification\": \"string (optional)\"\n" +
            "}";

    // 3. Coach System Prompt
    private static final String COACH_SYSTEM_PROMPT = 
            "You are a friendly and knowledgeable AI Health Coach. " +
            "Your goal is to provide helpful, science-backed advice on nutrition and wellness. " +
            "Do NOT attempt to calculate calories or log food. " +
            "Focus on answering questions, suggesting healthy alternatives, and motivation. " +
            "Keep answers concise and encouraging. " +
            "Always return valid JSON. " +
            "Schema: {\"message\": \"string\", \"is_logging_action\": boolean}";

    // 4. Coach User Prompt Template
    private static final String COACH_USER_PROMPT_TEMPLATE = 
            "User Context: %s\n\n" +
            "User Question: \"%s\"\n\n" +
            "Provide a helpful response.";

    // 3. Coach System Prompt
    private static final String COACH_SYSTEM_PROMPT = 
            "You are a friendly and knowledgeable AI Health Coach. " +
            "Your goal is to provide helpful, science-backed advice on nutrition and wellness. " +
            "Do NOT attempt to calculate calories or log food. " +
            "Focus on answering questions, suggesting healthy alternatives, and motivation. " +
            "Keep answers concise and encouraging. " +
            "Always return valid JSON. " +
            "Schema: {\"message\": \"string\", \"is_logging_action\": boolean}";

    // 4. Coach User Prompt Template
    private static final String COACH_USER_PROMPT_TEMPLATE = 
            "User Context: %s\n\n" +
            "User Question: \"%s\"\n\n" +
            "Provide a helpful response.";

    @org.springframework.beans.factory.annotation.Value("${pollinations.api-key}")
    private String apiKey;

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "nutritionCache", key = "#mealText.hashCode()")
    public AiNutritionResponse analyzeMeal(String mealText) throws JsonProcessingException {
        log.info("Analyzing meal (Cache Miss): {}", mealText.length() > 50 ? mealText.substring(0, 50) + "..." : mealText);
        // Build request body per OpenAI Chat Completion format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Add System Message
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // Add User Message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", String.format(USER_PROMPT_TEMPLATE, mealText));
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        // requestBody.put("temperature", 0.5); // Pollinations/Azure doesn't support temp < 1 for this model

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Sending request to Pollinations AI for meal: {}", mealText);
            Map<String, Object> response = restTemplate.postForObject(POLLINATIONS_URL, entity, Map.class);
            log.info("Received response from Pollinations AI: {}", response);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) messageObj.get("content");
                    log.info("Raw AI content: {}", content);
                    
                    // Clean markdown if present
                    content = cleanJsonContent(content);
                    log.info("Cleaned JSON content: {}", content);
                    
                    AiNutritionResponse aiResponse = objectMapper.readValue(content, AiNutritionResponse.class);
                    log.info("Parsed AI Response: {}", aiResponse);
                    return aiResponse;
                }
            }
        } catch (Exception e) {
            log.error("AI Service Error: {}", e.getMessage(), e);
            // Fallback to mock on failure
            return getMockResponse();
        }
        
        return getMockResponse();
    }

    private String cleanJsonContent(String content) {
        if (content == null) return "{}";
        content = content.replace("```json", "").replace("```", "").trim();
        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1) {
            return content.substring(jsonStart, jsonEnd + 1);
        }
        return content;
    }

    private AiNutritionResponse getMockResponse() {
        AiNutritionResponse response = new AiNutritionResponse();
        
        AiNutritionResponse.FoodItemDto item1 = new AiNutritionResponse.FoodItemDto("Roti (Mock)", "1 medium", 120, 3.0, 20.0, 2.0);
        AiNutritionResponse.FoodItemDto item2 = new AiNutritionResponse.FoodItemDto("Paneer (Mock)", "1 cup", 250, 10.0, 15.0, 18.0);
        
        response.setFoodItems(List.of(item1, item2));
        response.setTotals(new AiNutritionResponse.TotalsDto(370, 13.0, 35.0, 20.0));
        response.setConfidence("low");
        response.setClarification("AI Service unavailable, using mock data.");
        
        return response;
    }

    @Override
    public com.hoxlabs.calorieai.dto.ChatResponse getHealthAdvice(String userContext, String question) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System Message
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", COACH_SYSTEM_PROMPT);
        messages.add(systemMsg);

        // User Message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", String.format(COACH_USER_PROMPT_TEMPLATE, userContext, question));
        messages.add(userMsg);
        
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(POLLINATIONS_URL, entity, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) messageObj.get("content");
                    content = cleanJsonContent(content);
                    return objectMapper.readValue(content, com.hoxlabs.calorieai.dto.ChatResponse.class);
                }
            }
        } catch (Exception e) {
            log.error("AI Coach Error: {}", e.getMessage(), e);
            return new com.hoxlabs.calorieai.dto.ChatResponse("I'm having trouble connecting to my brain right now, but remember: consistency is key!", false);
        }
        return new com.hoxlabs.calorieai.dto.ChatResponse("I couldn't process that request.", false);
    }
}
