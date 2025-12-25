package com.hoxlabs.calorieai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;
import com.hoxlabs.calorieai.dto.MealLogRequest;
import com.hoxlabs.calorieai.dto.MealLogResponse;
import com.hoxlabs.calorieai.entity.MealType;
import com.hoxlabs.calorieai.service.MealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MealControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MealService mealService;

    @Test
    @WithMockUser
    void logMeal_ShouldReturnResponse_WhenUserIsAuthenticated() throws Exception {
        MealLogRequest request = new MealLogRequest();
        request.setText("Test Meal");
        request.setMealType(MealType.LUNCH);

        MealLogResponse response = MealLogResponse.builder()
                .id(1L)
                .text("Test Meal")
                .foodItems(List.of(new AiNutritionResponse.FoodItemDto("Test Food", 100, 10.0, 10.0, 5.0)))
                .totalCalories(100)
                .build();

        // Using any() string for email because @WithMockUser provides a default username "user"
        when(mealService.logMeal(any(String.class), any(MealLogRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/meals/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Test Meal"));
    }

    @Test
    void logMeal_ShouldReturnForbidden_WhenUserIsNotAuthenticated() throws Exception {
        MealLogRequest request = new MealLogRequest();
        request.setText("Test Meal");
        request.setMealType(MealType.LUNCH);

        mockMvc.perform(post("/api/meals/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void logMeal_ShouldReturnBadRequest_WhenInputIsInvalid() throws Exception {
        MealLogRequest request = new MealLogRequest();
        request.setText(""); // Invalid empty text
        request.setMealType(null); // Invalid null type

        mockMvc.perform(post("/api/meals/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
