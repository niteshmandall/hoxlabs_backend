package com.hoxlabs.calorieai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nutrition_summaries",
        uniqueConstraints = {
            @UniqueConstraint(name = "uc_summary_user_date", columnNames = {"user_id", "date"})
        })
public class NutritionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer totalCalories = 0;

    @Column(nullable = false)
    private Double totalProtein = 0.0;

    @Column(nullable = false)
    private Double totalCarbs = 0.0;

    @Column(nullable = false)
    private Double totalFat = 0.0;
}
