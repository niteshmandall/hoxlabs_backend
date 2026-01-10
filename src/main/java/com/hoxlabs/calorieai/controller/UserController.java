package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.dto.UpdateProfileRequest;
import com.hoxlabs.calorieai.dto.UserProfileDTO;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import com.hoxlabs.calorieai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final com.hoxlabs.calorieai.service.ImageService imageService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getUserProfile(email));


    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(authService.updateProfile(email, request));
    }

    @PostMapping(value = "/profile-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadProfileImage(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file
    ) throws java.io.IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        String imageUrl = imageService.saveProfileImage(file, user.getId());
        authService.updateProfilePhoto(email, imageUrl);
        
        return ResponseEntity.ok(java.util.Map.of("url", imageUrl));
    }
}
