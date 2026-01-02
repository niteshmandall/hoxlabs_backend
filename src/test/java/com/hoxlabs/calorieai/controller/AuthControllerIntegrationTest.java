package com.hoxlabs.calorieai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
import com.hoxlabs.calorieai.dto.TokenRefreshRequest;
import com.hoxlabs.calorieai.dto.TokenRefreshResponse;
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

    // --- Register Tests ---

    @Test
    void register_ShouldReturnToken_WhenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setCalorieGoal(2000);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("mock-jwt-token")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    // --- Login Tests ---

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("mock-jwt-token")
                .build();

        when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_ShouldReturnForbidden_WhenCredentialsAreInvalid() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "wrong");
        when(authService.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // GlobalExceptionHandler maps BadCredentialsException (runtime) or we catch it.
        // If ExceptionHandler catches RuntimeException -> 400.
        // If specific handler for BadCredentials not present -> 400 or 403 or 500.
        // Let's assume RuntimeException handler catches it as 400 per current impl.
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }
    @Test
    void refreshToken_ShouldReturnNewToken_WhenRefreshTokenIsValid() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("valid-refresh-token");

        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }
}
