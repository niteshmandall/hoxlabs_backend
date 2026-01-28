package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.service.AuthService;
import com.hoxlabs.calorieai.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = {
        "jwt.refresh-expiration=604800000",
        "jwt.expiration=900000"
})
class ProfileImageUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @MockBean
    private AuthService authService;

    @Autowired
    private com.hoxlabs.calorieai.repository.UserRepository userRepository;

    private Long testUserId;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userRepository.deleteAll();
        com.hoxlabs.calorieai.entity.User user = new com.hoxlabs.calorieai.entity.User();
        user.setEmail("test@example.com");
        user.setRole(com.hoxlabs.calorieai.entity.Role.USER);
        user = userRepository.save(user);
        testUserId = user.getId();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void uploadProfileImage_ShouldReturnUrl_WhenUploadIsSuccessful() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-binary-data".getBytes());

        when(imageService.saveProfileImage(any(), eq(testUserId)))
                .thenReturn("/uploads/users/" + testUserId + "/profile.jpg");

        mockMvc.perform(multipart("/api/user/profile-image")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/uploads/users/" + testUserId + "/profile.jpg"));

        // Verify interactions
        verify(imageService).saveProfileImage(any(), eq(testUserId));
        verify(authService).updateProfilePhoto(eq("test@example.com"),
                eq("/uploads/users/" + testUserId + "/profile.jpg"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void uploadProfileImage_ShouldReturn500_WhenUploadFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-binary-data".getBytes());

        when(imageService.saveProfileImage(any(), eq(testUserId))).thenThrow(new java.io.IOException("Disk full"));

        mockMvc.perform(multipart("/api/user/profile-image")
                .file(file))
                .andExpect(status().isInternalServerError());
    }
}
