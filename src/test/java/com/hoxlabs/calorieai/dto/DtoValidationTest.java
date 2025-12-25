package com.hoxlabs.calorieai.dto;

import com.hoxlabs.calorieai.entity.MealType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void mealLogRequest_ShouldFail_WhenTextIsBlank() {
        MealLogRequest request = new MealLogRequest();
        request.setText("");
        request.setMealType(MealType.BREAKFAST);

        Set<ConstraintViolation<MealLogRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Text cannot be blank")));
    }

    @Test
    void mealLogRequest_ShouldFail_WhenMealTypeIsNull() {
        MealLogRequest request = new MealLogRequest();
        request.setText("Valid Text");
        request.setMealType(null);

        Set<ConstraintViolation<MealLogRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Meal type is required")));
    }

    @Test
    void mealLogRequest_ShouldPass_WhenValid() {
        MealLogRequest request = new MealLogRequest();
        request.setText("Valid Text");
        request.setMealType(MealType.DINNER);

        Set<ConstraintViolation<MealLogRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
