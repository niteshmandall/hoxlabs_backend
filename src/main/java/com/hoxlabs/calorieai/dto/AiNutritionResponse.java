package com.hoxlabs.calorieai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiNutritionResponse {
    
    // Support both "foodItems" (legacy) and "items" (new prompt)
    @JsonAlias("items")
    private List<FoodItemDto> foodItems;

    private TotalsDto totals;
    private String confidence; // high, medium, low
    private String clarification; // For vague inputs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private String name;
        private String quantity; // New field from prompt
        private Integer calories;
        private Double protein;
        private Double carbs;
        private Double fat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalsDto {
        private Integer calories;
        private Double protein;
        private Double carbs;
        private Double fat;
    }
}
