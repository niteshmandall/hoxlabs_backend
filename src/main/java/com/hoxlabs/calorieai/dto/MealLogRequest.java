package com.hoxlabs.calorieai.dto;

import com.hoxlabs.calorieai.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealLogRequest {
    private String text;
    private MealType mealType;
}
