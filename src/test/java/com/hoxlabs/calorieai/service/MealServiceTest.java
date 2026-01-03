package com.hoxlabs.calorieai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.dto.MealLogRequest;
import com.hoxlabs.calorieai.dto.MealLogResponse;
import com.hoxlabs.calorieai.entity.MealLog;
import com.hoxlabs.calorieai.entity.MealType;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.FoodItemRepository;
import com.hoxlabs.calorieai.repository.MealLogRepository;
import com.hoxlabs.calorieai.repository.NutritionSummaryRepository;
import com.hoxlabs.calorieai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock private MealLogRepository mealLogRepository;
    @Mock private FoodItemRepository foodItemRepository;
    @Mock private NutritionSummaryRepository nutritionSummaryRepository;
    @Mock private UserRepository userRepository;
    @Mock private AiNutritionService aiNutritionService;

    @InjectMocks
    private MealService mealService;

    // --- Log Meal Success ---

    @Test
    void logMeal_ShouldSaveLogAndItems_WhenUserExists() throws JsonProcessingException {
        // Setup
        org.springframework.test.util.ReflectionTestUtils.setField(mealService, "apiKey", "test-key");
        
        String email = "test@example.com";
        User user = new User(); user.setId(1L); user.setEmail(email);
        MealLogRequest req = new MealLogRequest("Test", MealType.LUNCH);
        
        AiNutritionResponse aiRes = new AiNutritionResponse();
        aiRes.setFoodItems(List.of(new AiNutritionResponse.FoodItemDto("Food", "1 serving", 100, 10.0, 10.0, 5.0)));
        
        MealLog savedLog = new MealLog();
        savedLog.setId(100L);
        savedLog.setRawText("Test");
        savedLog.setMealType(MealType.LUNCH);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(aiNutritionService.analyzeMeal("Test")).thenReturn(aiRes);
        when(mealLogRepository.save(any())).thenReturn(savedLog);
        
        // Execute
        MealLogResponse res = mealService.logMeal(email, req);
        
        // Verify
        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals(100, res.getTotalCalories());
        
        // strict check for image url generation
        org.mockito.ArgumentCaptor<MealLog> logCaptor = org.mockito.ArgumentCaptor.forClass(MealLog.class);
        verify(mealLogRepository).save(logCaptor.capture());
        MealLog capturedLog = logCaptor.getValue();
        // Prompt is now prefixed
        assertEquals("https://image.pollinations.ai/prompt/Delicious+food+photography+of+Test?model=turbo&width=1024&height=1024&enhance=true&nologo=true&api_key=test-key", capturedLog.getImageUrl());

        verify(foodItemRepository).saveAll(anyList());
        verify(nutritionSummaryRepository).findByUserIdAndDate(eq(1L), any(LocalDate.class));
    }

    // --- Log Meal Failures ---

    @Test
    void logMeal_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("miss@test.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> mealService.logMeal("miss@test.com", new MealLogRequest()));
    }

    @Test
    void logMeal_ShouldPropagateException_WhenAiServiceFails() throws JsonProcessingException {
        User user = new User(); user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        when(aiNutritionService.analyzeMeal(any())).thenThrow(new RuntimeException("AI Down"));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> mealService.logMeal("test@example.com", new MealLogRequest("Text", MealType.SNACK)));
        assertEquals("AI Down", ex.getMessage());
    }

    @Test
    void logMeal_ShouldHandleEmptyFoodItems() throws JsonProcessingException {
        String email = "test@example.com";
        User user = new User(); user.setId(1L); user.setEmail(email);
        
        AiNutritionResponse aiRes = new AiNutritionResponse();
        aiRes.setFoodItems(Collections.emptyList()); // Empty
        
        MealLog savedLog = new MealLog();
        savedLog.setId(100L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(aiNutritionService.analyzeMeal(any())).thenReturn(aiRes);
        when(mealLogRepository.save(any())).thenReturn(savedLog);

        MealLogResponse res = mealService.logMeal(email, new MealLogRequest("Text", MealType.SNACK));
        
        assertNotNull(res);
        assertEquals(0, res.getTotalCalories());
        assertNull(res.getImageUrl(), "Image URL should be null when no food items are found");
        verify(foodItemRepository).saveAll(anyList()); // Validates it doesn't crash on empty list
    }
    
    @Test
    void logMeal_ShouldThrowException_WhenDbSaveFails() throws Exception {
        User user = new User(); user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(aiNutritionService.analyzeMeal(any())).thenReturn(new AiNutritionResponse());
        
        when(mealLogRepository.save(any())).thenThrow(new DataIntegrityViolationException("DB Error"));
        
        assertThrows(DataIntegrityViolationException.class, () -> mealService.logMeal("test@example.com", new MealLogRequest("Text", MealType.SNACK)));
    }

    // --- Get Meal History ---

    @Test
    void getMealHistory_ShouldReturnLogs_WhenUserExists() {
        // Setup
        String email = "history@example.com";
        User user = new User(); user.setEmail(email);
        
        MealLog log1 = new MealLog();
        log1.setId(1L);
        log1.setRawText("Meal 1");
        log1.setTimestamp(java.time.LocalDateTime.now());
        log1.setImageUrl("http://img.url");
        log1.setFoodItems(Collections.emptyList());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mealLogRepository.findAllByUserOrderByTimestampDesc(user)).thenReturn(List.of(log1));

        // Execute
        List<MealLogResponse> history = mealService.getMealHistory(email);

        // Verify
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("Meal 1", history.get(0).getText());
        assertEquals("http://img.url", history.get(0).getImageUrl());
        verify(mealLogRepository).findAllByUserOrderByTimestampDesc(user);
    }
}
