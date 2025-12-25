package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import com.hoxlabs.calorieai.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // --- Register Tests ---

    @Test
    void register_ShouldReturnToken_WhenUserIsNew() {
        RegisterRequest request = new RegisterRequest("new@test.com", "pass", 2000);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtUtil.generateToken(any())).thenReturn("jwt");
        when(userRepository.save(any())).thenReturn(new User());

        AuthenticationResponse res = authService.register(request);
        assertNotNull(res);
        assertEquals("jwt", res.getToken());
    }

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        RegisterRequest request = new RegisterRequest("exist@test.com", "pass", 2000);
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    // --- Login Tests ---

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsValid() {
        AuthenticationRequest req = new AuthenticationRequest("user@test.com", "pass");
        User user = new User();
        user.setEmail("user@test.com");
        
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt");

        AuthenticationResponse res = authService.authenticate(req);
        assertNotNull(res);
        assertEquals("jwt", res.getToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        AuthenticationRequest req = new AuthenticationRequest("missing@test.com", "pass");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.authenticate(req));
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsString_WhenAuthManagerFails() {
        AuthenticationRequest req = new AuthenticationRequest("user@test.com", "wrongpass");
        
        doThrow(new BadCredentialsException("Bad creds")).when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(req));
    }

    @Test
    void authenticate_ShouldThrowLocked_WhenAccountLocked() {
        AuthenticationRequest req = new AuthenticationRequest("locked@test.com", "pass");
        
        doThrow(new LockedException("Locked")).when(authenticationManager).authenticate(any());

        assertThrows(LockedException.class, () -> authService.authenticate(req));
    }

    @Test
    void authenticate_ShouldThrowDisabled_WhenAccountDisabled() {
        AuthenticationRequest req = new AuthenticationRequest("disabled@test.com", "pass");
        
        doThrow(new DisabledException("Disabled")).when(authenticationManager).authenticate(any());

        assertThrows(DisabledException.class, () -> authService.authenticate(req));
    }
}
