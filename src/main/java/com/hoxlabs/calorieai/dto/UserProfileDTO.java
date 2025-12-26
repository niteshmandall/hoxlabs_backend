package com.hoxlabs.calorieai.dto;

import com.hoxlabs.calorieai.entity.FitnessGoal;
import com.hoxlabs.calorieai.entity.Gender;
import com.hoxlabs.calorieai.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private String email;
    private String name;
    private Integer age;
    private Gender gender;
    private Float weight;
    private Float height;
    private FitnessGoal fitnessGoal;
    private Integer calorieGoal;
    private Role role;
}
