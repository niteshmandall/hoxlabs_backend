package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.entity.NutritionSummary;
import com.hoxlabs.calorieai.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser
    void getDailySummary_ShouldReturnSummary_WhenUserIsAuthenticated() throws Exception {
        NutritionSummary summary = new NutritionSummary();
        summary.setTotalCalories(2000);
        summary.setTotalProtein(150.0);
        summary.setTotalCarbs(200.0);
        summary.setTotalFat(70.0);

        when(dashboardService.getDailySummary(any(String.class), any(LocalDate.class)))
                .thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(2000))
                .andExpect(jsonPath("$.totalProtein").value(150.0));
    }

    @Test
    void getDailySummary_ShouldReturnForbidden_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
