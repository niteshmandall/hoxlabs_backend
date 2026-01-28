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
            @AuthenticationPrincipal String email,
            @RequestBody @Valid MealLogRequest request) throws JsonProcessingException {
        return ResponseEntity.ok(mealService.logMeal(email, request));
    }

    @GetMapping("/history")
    public ResponseEntity<java.util.List<MealLogResponse>> getMealHistory(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(mealService.getMealHistory(email, date));
        }
        return ResponseEntity.ok(mealService.getMealHistory(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        mealService.deleteMeal(id, email);
        return ResponseEntity.ok().build();
    }
}
