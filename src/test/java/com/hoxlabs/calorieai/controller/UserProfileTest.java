package com.hoxlabs.calorieai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoxlabs.calorieai.dto.UpdateProfileRequest;
import com.hoxlabs.calorieai.dto.UserProfileDTO;
import com.hoxlabs.calorieai.service.AuthService;
import com.hoxlabs.calorieai.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        // We need to verify that fetching profile uses UserRepository directly in
        // controller
        // or calls a service. The controller calls userRepository directly for GET.
        // So we need to populate data in DB or spy userRepository.
        // However, @SpringBootTest uses the real DB (H2).
        // So we should save a user first.

        @Autowired
        private com.hoxlabs.calorieai.repository.UserRepository userRepository;

        @MockBean
        private AuthService authService; // For updateProfile

        @MockBean
        private ImageService imageService; // Required bean

        @Test
        @WithMockUser(username = "profileuser@test.com")
        void getUserProfile_ShouldReturnProfile_WhenUserExists() throws Exception {
                // Setup User
                com.hoxlabs.calorieai.entity.User user = new com.hoxlabs.calorieai.entity.User();
                user.setEmail("profileuser@test.com");
                user.setName("Test User");
                user.setRole(com.hoxlabs.calorieai.entity.Role.USER);
                user.setProfilePhotoUrl("http://example.com/photo.jpg");
                userRepository.save(user);

                mockMvc.perform(get("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("profileuser@test.com"))
                                .andExpect(jsonPath("$.name").value("Test User"))
                                .andExpect(jsonPath("$.profilePhotoUrl").value("http://example.com/photo.jpg"));
        }

        @Test
        @WithMockUser(username = "profileuser@test.com")
        void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
                UserProfileDTO updatedProfile = UserProfileDTO.builder()
                                .name("Updated Name")
                                .email("profileuser@test.com")
                                .build();

                when(authService.updateProfile(eq("profileuser@test.com"), any(UpdateProfileRequest.class)))
                                .thenReturn(updatedProfile);

                UpdateProfileRequest request = new UpdateProfileRequest();
                request.setName("Updated Name");

                mockMvc.perform(put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Name"));
        }
}
