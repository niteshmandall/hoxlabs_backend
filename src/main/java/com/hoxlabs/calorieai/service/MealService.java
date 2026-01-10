package com.hoxlabs.calorieai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.dto.MealLogRequest;
import com.hoxlabs.calorieai.dto.MealLogResponse;
import com.hoxlabs.calorieai.entity.*;
import com.hoxlabs.calorieai.repository.FoodItemRepository;
import com.hoxlabs.calorieai.repository.MealLogRepository;
import com.hoxlabs.calorieai.repository.NutritionSummaryRepository;
import com.hoxlabs.calorieai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealService {

    private final MealLogRepository mealLogRepository;
    private final FoodItemRepository foodItemRepository;
    private final NutritionSummaryRepository nutritionSummaryRepository;
    private final UserRepository userRepository;
    private final AiNutritionService aiNutritionService;
    private final org.springframework.cache.CacheManager cacheManager;

    @org.springframework.beans.factory.annotation.Value("${pollinations.api-key}")
    private String apiKey;

    @Transactional
    public MealLogResponse logMeal(String userEmail, MealLogRequest request) throws JsonProcessingException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Analyze with AI (Synchronous)
        log.info("Analyzing meal for user: {}, text: {}", userEmail, request.getText());
        AiNutritionResponse aiResponse = aiNutritionService.analyzeMeal(request.getText());
        int itemCount = (aiResponse.getFoodItems() != null) ? aiResponse.getFoodItems().size() : 0;
        log.info("AI Analysis complete. Found {} items. Confidence: {}", itemCount, aiResponse.getConfidence());

        // 2. Save Meal Log
        MealLog mealLog = new MealLog();
        mealLog.setUser(user);
        mealLog.setMealType(request.getMealType());
        mealLog.setTimestamp(LocalDateTime.now());
        mealLog.setRawText(request.getText());
        
        // Generate Image URL (Instant)
        if (request.getText() != null && !request.getText().isEmpty()) {
            String prompt = "Delicious food photography of " + request.getText();
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            // Enhanced Image Generation URL with Turbo model + API Key
            String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + 
                              "?model=turbo&width=1024&height=1024&enhance=true&nologo=true&api_key=" + apiKey;
            mealLog.setImageUrl(imageUrl);
        }
        
        mealLog = mealLogRepository.save(mealLog);

        // 3. Save Food Items
        List<FoodItem> foodItems = new ArrayList<>();
        int totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        if (aiResponse.getFoodItems() != null) {
            for (AiNutritionResponse.FoodItemDto dto : aiResponse.getFoodItems()) {
                FoodItem item = new FoodItem();
                item.setMealLog(mealLog);
                item.setName(dto.getName());
                item.setCalories(dto.getCalories());
                item.setProtein(dto.getProtein());
                item.setCarbs(dto.getCarbs());
                item.setFat(dto.getFat());
                foodItems.add(item);
    
                totalCalories += dto.getCalories();
                totalProtein += dto.getProtein();
                totalCarbs += dto.getCarbs();
                totalFat += dto.getFat();
            }
            foodItemRepository.saveAll(foodItems);
        }

        // 4. Update Nutrition Summary
        updateNutritionSummary(user, totalCalories, totalProtein, totalCarbs, totalFat);

        // 5. Build Response
        MealLogResponse response = MealLogResponse.builder()
                .id(mealLog.getId())
                .text(mealLog.getRawText())
                .imageUrl(mealLog.getImageUrl())
                .timestamp(mealLog.getTimestamp())
                .foodItems(aiResponse.getFoodItems())
                .totalCalories(totalCalories)
                .build();
                
        // Evict Caches
        evictCaches(user.getEmail(), LocalDate.now());
        
        return response;
    }

    private void evictCaches(String email, LocalDate date) {
        if (cacheManager.getCache("mealHistory") != null) {
            cacheManager.getCache("mealHistory").evict(email);
        }
        if (cacheManager.getCache("dailySummary") != null) {
            cacheManager.getCache("dailySummary").evict(email + "_" + date);
        }
    }

    private void updateNutritionSummary(User user, int calories, double protein, double carbs, double fat) {
        LocalDate today = LocalDate.now();
        NutritionSummary summary = nutritionSummaryRepository.findByUserIdAndDate(user.getId(), today)
                .orElse(new NutritionSummary(null, user, today, 0, 0.0, 0.0, 0.0));

        summary.setTotalCalories(summary.getTotalCalories() + calories);
        summary.setTotalProtein(summary.getTotalProtein() + protein);
        summary.setTotalCarbs(summary.getTotalCarbs() + carbs);
        summary.setTotalFat(summary.getTotalFat() + fat);

        nutritionSummaryRepository.save(summary);
    }
    

    
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "mealHistory", key = "#userEmail")
    public List<MealLogResponse> getMealHistory(String userEmail) {
         User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

         List<MealLog> logs = mealLogRepository.findAllByUserOrderByTimestampDesc(user);
         
         return logs.stream().map(log -> {
             List<AiNutritionResponse.FoodItemDto> itemDtos = log.getFoodItems().stream()
                     .map(item -> new AiNutritionResponse.FoodItemDto(
                             item.getName(), 
                             "1 serving", // Default quantity as it's not in FoodItem entity yet, or handled elsewhere
                             item.getCalories(), 
                             item.getProtein(), 
                             item.getCarbs(), 
                             item.getFat()
                     ))
                     .collect(Collectors.toList());

             int totalCals = itemDtos.stream().mapToInt(AiNutritionResponse.FoodItemDto::getCalories).sum();

             return MealLogResponse.builder()
                     .id(log.getId())
                     .text(log.getRawText())
                     .imageUrl(log.getImageUrl())
                     .timestamp(log.getTimestamp())
                     .foodItems(itemDtos)
                     .totalCalories(totalCals)
                     .build();
         }).collect(Collectors.toList());
    }
    @Transactional
    public void deleteMeal(Long mealId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MealLog mealLog = mealLogRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        if (!mealLog.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only delete your own meals");
        }

        mealLogRepository.delete(mealLog);
        
        // Evict Caches
        evictCaches(userEmail, mealLog.getTimestamp().toLocalDate());
    }
}
