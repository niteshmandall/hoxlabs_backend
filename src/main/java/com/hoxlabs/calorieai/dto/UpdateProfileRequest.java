package com.hoxlabs.calorieai.dto;

import com.hoxlabs.calorieai.entity.FitnessGoal;
import com.hoxlabs.calorieai.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String name;
    private Integer age;
    private Gender gender;
    private Float weight;
    private Float height;
    private FitnessGoal fitnessGoal;
    
    private String profilePhotoUrl;
    private Integer dailyCalorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;
}
