package com.hoxlabs.calorieai.repository;

import com.hoxlabs.calorieai.entity.NutritionSummary;
import com.hoxlabs.calorieai.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NutritionSummaryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NutritionSummaryRepository repository;

    @Test
    void findByUserIdAndDate_ShouldReturnSummary_WhenExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCalorieGoal(2000);
        entityManager.persist(user);

        LocalDate date = LocalDate.now();
        NutritionSummary summary = new NutritionSummary();
        summary.setUser(user);
        summary.setDate(date);
        summary.setTotalCalories(500);
        summary.setTotalProtein(20.0);
        summary.setTotalCarbs(50.0);
        summary.setTotalFat(10.0);
        entityManager.persist(summary);
        entityManager.flush();

        Optional<NutritionSummary> found = repository.findByUserIdAndDate(user.getId(), date);

        assertThat(found).isPresent();
        assertThat(found.get().getTotalCalories()).isEqualTo(500);
    }

    @Test
    void findByUserIdAndDate_ShouldReturnEmpty_WhenNotExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCalorieGoal(2000);
        entityManager.persist(user);
        entityManager.flush();

        Optional<NutritionSummary> found = repository.findByUserIdAndDate(user.getId(), LocalDate.now());

        assertThat(found).isEmpty();
    }
}
