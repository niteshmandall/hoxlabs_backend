package com.hoxlabs.calorieai.controller;

import com.hoxlabs.calorieai.dto.UserProfileDTO;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserProfileDTO profile = UserProfileDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .weight(user.getWeight())
                .height(user.getHeight())
                .fitnessGoal(user.getFitnessGoal())
                .calorieGoal(user.getCalorieGoal())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(profile);
    }
}
