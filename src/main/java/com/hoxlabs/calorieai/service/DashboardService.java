package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.entity.NutritionSummary;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.NutritionSummaryRepository;
import com.hoxlabs.calorieai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final NutritionSummaryRepository nutritionSummaryRepository;
    private final UserRepository userRepository;

    @org.springframework.cache.annotation.Cacheable(value = "dailySummary", key = "#userEmail + '_' + #date")
    public NutritionSummary getDailySummary(String userEmail, LocalDate date) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return nutritionSummaryRepository.findByUserIdAndDate(user.getId(), date)
                .orElse(new NutritionSummary(null, user, date, 0, 0.0, 0.0, 0.0));
    }
}
