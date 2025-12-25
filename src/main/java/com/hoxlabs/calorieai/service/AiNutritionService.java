package com.hoxlabs.calorieai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoxlabs.calorieai.dto.AiNutritionResponse;

public interface AiNutritionService {
    AiNutritionResponse analyzeMeal(String mealText) throws JsonProcessingException;
}
