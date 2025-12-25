package com.hoxlabs.calorieai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoxlabs.calorieai.dto.MealLogRequest;
import com.hoxlabs.calorieai.dto.MealLogResponse;
import com.hoxlabs.calorieai.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @PostMapping("/log")
    public ResponseEntity<MealLogResponse> logMeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MealLogRequest request
    ) throws JsonProcessingException {
        return ResponseEntity.ok(mealService.logMeal(userDetails.getUsername(), request));
    }
}
