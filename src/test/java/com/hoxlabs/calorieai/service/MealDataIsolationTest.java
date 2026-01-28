package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.dto.MealLogRequest;
import com.hoxlabs.calorieai.dto.MealLogResponse;
import com.hoxlabs.calorieai.entity.MealType;
import com.hoxlabs.calorieai.entity.Role;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.MealLogRepository;
import com.hoxlabs.calorieai.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class MealDataIsolationTest {

    @Autowired
    private MealService mealService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MealLogRepository mealLogRepository;

    @MockBean
    private AiNutritionService aiNutritionService;

    @Test
    void testMealHistoryIsolation() throws Exception {
        // 1. Create User A and User B
        User userA = new User("userA@example.com", Role.USER, 2000, "uidA");
        userRepository.save(userA);

        User userB = new User("userB@example.com", Role.USER, 2000, "uidB");
        userRepository.save(userB);

        // Mock AI Service
        when(aiNutritionService.analyzeMeal(anyString())).thenReturn(new AiNutritionResponse(Collections.emptyList(), new AiNutritionResponse.TotalsDto(100, 10.0, 10.0, 5.0), "high", null));

        // 2. Log Meal for User A
        MealLogRequest reqA = new MealLogRequest("Meal A", MealType.LUNCH, LocalDate.now());
        mealService.logMeal(userA.getEmail(), reqA);

        // 3. Log Meal for User B
        MealLogRequest reqB = new MealLogRequest("Meal B", MealType.DINNER, LocalDate.now());
        mealService.logMeal(userB.getEmail(), reqB);

        // 4. Fetch History for User A
        List<MealLogResponse> historyA = mealService.getMealHistory(userA.getEmail());

        // 5. Assertions
        Assertions.assertEquals(1, historyA.size(), "User A should see exactly 1 meal. Found: " + historyA.size());
        Assertions.assertEquals("Meal A", historyA.get(0).getText(), "User A should see their own meal");

        // 6. Fetch History for User B
        List<MealLogResponse> historyB = mealService.getMealHistory(userB.getEmail());
        Assertions.assertEquals(1, historyB.size(), "User B should see exactly 1 meal");
        Assertions.assertEquals("Meal B", historyB.get(0).getText());
    }
}
