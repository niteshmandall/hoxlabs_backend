package com.hoxlabs.calorieai.repository;

import com.hoxlabs.calorieai.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByUserId(Long userId);

    List<MealLog> findAllByUserOrderByTimestampDesc(com.hoxlabs.calorieai.entity.User user);

    List<MealLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<MealLog> findAllByUserAndTimestampBetween(com.hoxlabs.calorieai.entity.User user, LocalDateTime start,
            LocalDateTime end);
}
