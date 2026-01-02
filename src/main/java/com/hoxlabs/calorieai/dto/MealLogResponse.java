package com.hoxlabs.calorieai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealLogResponse {
    private Long id;
    private String text;
    private String imageUrl;
    private LocalDateTime timestamp;
    private List<AiNutritionResponse.FoodItemDto> foodItems;
    private Integer totalCalories;
}
