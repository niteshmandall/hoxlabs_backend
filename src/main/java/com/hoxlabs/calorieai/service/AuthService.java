package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
import com.hoxlabs.calorieai.dto.UserProfileDTO;
import com.hoxlabs.calorieai.entity.Role;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import com.hoxlabs.calorieai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        var user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER,
                request.getCalorieGoal()
        );
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setWeight(request.getWeight());
        user.setHeight(request.getHeight());
        user.setFitnessGoal(request.getFitnessGoal());

        userRepository.save(user);
        var jwtToken = jwtUtil.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId()); // Create Refresh Token

        var userProfile = UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .weight(user.getWeight())
                .height(user.getHeight())
                .fitnessGoal(user.getFitnessGoal())
                .calorieGoal(user.getCalorieGoal())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .proteinGoal(user.getProteinGoal())
                .carbsGoal(user.getCarbsGoal())
                .fatGoal(user.getFatGoal())
                .role(user.getRole())
                .build();

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userProfile)
                .build();
    }
    
    // updateProfile method (no changes needed)

    public UserProfileDTO updateProfile(String email, com.hoxlabs.calorieai.dto.UpdateProfileRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getWeight() != null) user.setWeight(request.getWeight());
        if (request.getHeight() != null) user.setHeight(request.getHeight());
        if (request.getFitnessGoal() != null) user.setFitnessGoal(request.getFitnessGoal());
        if (request.getDailyCalorieGoal() != null) user.setCalorieGoal(request.getDailyCalorieGoal());
        
        if (request.getProfilePhotoUrl() != null) user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        if (request.getProteinGoal() != null) user.setProteinGoal(request.getProteinGoal());
        if (request.getCarbsGoal() != null) user.setCarbsGoal(request.getCarbsGoal());
        if (request.getFatGoal() != null) user.setFatGoal(request.getFatGoal());

        user = userRepository.save(user);

        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .weight(user.getWeight())
                .height(user.getHeight())
                .fitnessGoal(user.getFitnessGoal())
                .calorieGoal(user.getCalorieGoal())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .proteinGoal(user.getProteinGoal())
                .carbsGoal(user.getCarbsGoal())
                .fatGoal(user.getFatGoal())
                .role(user.getRole())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtUtil.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId()); // Create Refresh Token
        
        var userProfile = UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .weight(user.getWeight())
                .height(user.getHeight())
                .fitnessGoal(user.getFitnessGoal())
                .calorieGoal(user.getCalorieGoal())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .proteinGoal(user.getProteinGoal())
                .carbsGoal(user.getCarbsGoal())
                .fatGoal(user.getFatGoal())
                .role(user.getRole())
                .build();

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userProfile)
                .build();
    }

    public com.hoxlabs.calorieai.dto.TokenRefreshResponse refreshToken(com.hoxlabs.calorieai.dto.TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(com.hoxlabs.calorieai.entity.RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    return com.hoxlabs.calorieai.dto.TokenRefreshResponse.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
}
