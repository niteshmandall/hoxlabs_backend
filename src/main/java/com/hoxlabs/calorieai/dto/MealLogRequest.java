package com.hoxlabs.calorieai.dto;

import com.hoxlabs.calorieai.entity.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealLogRequest {
    @NotBlank(message = "Text cannot be blank")
    private String text;

    @NotNull(message = "Meal type is required")
    private MealType mealType;
}
