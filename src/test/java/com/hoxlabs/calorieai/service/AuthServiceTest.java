package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
import com.hoxlabs.calorieai.entity.Role;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import com.hoxlabs.calorieai.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnToken_WhenUserIsNotDuplicate() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setCalorieGoal(2000);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        AuthenticationResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("wrong@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.authenticate(request));
    }
}
