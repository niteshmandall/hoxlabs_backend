package com.hoxlabs.calorieai.repository;

import com.hoxlabs.calorieai.entity.NutritionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NutritionSummaryRepository extends JpaRepository<NutritionSummary, Long> {
    Optional<NutritionSummary> findByUserIdAndDate(Long userId, LocalDate date);
}
