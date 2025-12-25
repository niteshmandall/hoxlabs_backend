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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealLogRepository mealLogRepository;
    @Mock
    private FoodItemRepository foodItemRepository;
    @Mock
    private NutritionSummaryRepository nutritionSummaryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AiNutritionService aiNutritionService;

    @InjectMocks
    private MealService mealService;

    @Test
    void logMeal_ShouldSaveLogAndItems_WhenUserExists() throws JsonProcessingException {
        String email = "test@example.com";
        Long userId = 1L;
        MealLogRequest request = new MealLogRequest();
        request.setText("Test Meal");
        request.setMealType(MealType.LUNCH);

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        AiNutritionResponse aiResponse = new AiNutritionResponse();
        AiNutritionResponse.FoodItemDto foodDto = new AiNutritionResponse.FoodItemDto("Test Food", 100, 10.0, 10.0, 5.0);
        aiResponse.setFoodItems(List.of(foodDto));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(aiNutritionService.analyzeMeal(request.getText())).thenReturn(aiResponse);

        MealLog savedLog = new MealLog();
        savedLog.setId(100L);
        savedLog.setUser(user);
        savedLog.setMealType(request.getMealType());
        savedLog.setRawText(request.getText());

        when(mealLogRepository.save(any(MealLog.class))).thenReturn(savedLog);

        MealLogResponse response = mealService.logMeal(email, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Test Meal", response.getText());

        verify(mealLogRepository).save(any(MealLog.class));
        verify(foodItemRepository).saveAll(anyList());
        verify(nutritionSummaryRepository).findByUserIdAndDate(eq(userId), any(LocalDate.class));
    }

    @Test
    void logMeal_ShouldThrowException_WhenUserNotFound() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> mealService.logMeal(email, new MealLogRequest()));
    }
}
