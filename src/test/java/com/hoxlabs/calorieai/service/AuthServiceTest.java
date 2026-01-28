package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    // --- Sync Tests ---

    @Test
    void syncUser_ShouldUpdateUid_WhenUserExists() {
        String email = "test@example.com";
        String uid = "firebase-uid";
        com.hoxlabs.calorieai.dto.SyncUserRequest req = new com.hoxlabs.calorieai.dto.SyncUserRequest();

        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = authService.syncUser(email, uid, req);

        assertEquals(uid, result.getFirebaseUid());
        verify(userRepository).save(existingUser);
    }

    @Test
    void syncUser_ShouldCreateUser_WhenUserIsNew() {
        String email = "new@example.com";
        String uid = "new-uid";
        com.hoxlabs.calorieai.dto.SyncUserRequest req = new com.hoxlabs.calorieai.dto.SyncUserRequest();
        req.setDailyCalorieGoal(2500);
        req.setName("New User");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = authService.syncUser(email, uid, req);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(uid, result.getFirebaseUid());
        assertEquals("New User", result.getName());
        assertEquals(2500, result.getCalorieGoal());
        verify(userRepository).save(any(User.class));
    }

    // --- Profile Tests (Keep existing logic if needed) ---

    @Test
    void getUserProfile_ShouldReturnProfile() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setCalorieGoal(2000);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var profile = authService.getUserProfile(email);
        assertEquals(email, profile.getEmail());
    }
}
