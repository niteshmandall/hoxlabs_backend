package com.hoxlabs.calorieai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        // --- Sync Tests ---

        @Test
        @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
        void syncUser_ShouldReturnProfile_WhenAuthenticated() throws Exception {
                com.hoxlabs.calorieai.dto.SyncUserRequest request = new com.hoxlabs.calorieai.dto.SyncUserRequest();
                request.setName("Synced User");
                request.setDailyCalorieGoal(2000);

                com.hoxlabs.calorieai.dto.UserProfileDTO response = com.hoxlabs.calorieai.dto.UserProfileDTO.builder()
                                .email("test@example.com")
                                .name("Synced User")
                                .build();

                // Mock AuthService logic
                // Verify syncUser is called with correct email, uid, request
                // We assume principal is email from WithMockUser unless Filter changes it.
                // Filter is NOT part of WithMockUser context usually, but Controller gets
                // principal from context.
                // Our controller casts principal to String. WithMockUser sets UserDetails or
                // String?
                // WithMockUser sets UserDetails usually.
                // Let's verify what Principal is in controller.

                // Controller: String emailOrUid = (String) principal;
                // If WithMockUser sets UserDetails, cast to String will fail!

                // FIX: Update Controller to handle Principal type safely OR update Test to set
                // String principal.
                // Or update SecurityConfig to use String principal.

                // In FirebaseTokenFilter we set: new
                // UsernamePasswordAuthenticationToken(email/uid, ...)
                // so principal is String.
                // In @WithMockUser, principal is User (UserDetails).

                // So @WithMockUser will break the controller if controller blindly casts to
                // String.

                // Controller fix: Use .toString() or check instanceof.
                // Or use @WithSecurityContext with data matching FirebaseTokenFilter.
                // Simpler: Fix Controller to be robust.

                when(authService.syncUser(any(), any(), any())).thenReturn(new com.hoxlabs.calorieai.entity.User());
                when(authService.getUserProfile(any())).thenReturn(response);

                mockMvc.perform(post("/api/auth/sync")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }
}
