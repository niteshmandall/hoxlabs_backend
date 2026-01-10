package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.dto.ChatRequest;
import com.hoxlabs.calorieai.dto.ChatResponse;
import com.hoxlabs.calorieai.dto.MealLogResponse; // Reusing for history
import com.hoxlabs.calorieai.service.AiNutritionService;
import com.hoxlabs.calorieai.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiNutritionService aiNutritionService;
    private final MealService mealService;

    @PostMapping("/advice")
    public ResponseEntity<ChatResponse> getAdvice(@RequestBody ChatRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 1. Get recent context (yesterday/today)
        List<MealLogResponse> history = mealService.getMealHistory(email);
        String context = "User history (last few meals): " + 
                history.stream().limit(5).map(m -> m.getText() + " (" + m.getTotalCalories() + " kcals)").collect(Collectors.joining(", "));

        // 2. Call AI Coach
        ChatResponse response = aiNutritionService.getHealthAdvice(context, request.getMessage());
        return ResponseEntity.ok(response);
    }
}
