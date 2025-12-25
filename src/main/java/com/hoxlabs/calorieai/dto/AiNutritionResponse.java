package com.hoxlabs.calorieai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiNutritionResponse {
    private List<FoodItemDto> foodItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private String name;
        private Integer calories;
        private Double protein;
        private Double carbs;
        private Double fat;
    }
}
