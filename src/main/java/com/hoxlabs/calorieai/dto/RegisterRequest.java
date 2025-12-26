package com.hoxlabs.calorieai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private Integer calorieGoal;
    private String name;
    private Integer age;
    private com.hoxlabs.calorieai.entity.Gender gender;
    private Float weight;
    private Float height;
    private com.hoxlabs.calorieai.entity.FitnessGoal fitnessGoal;
}
