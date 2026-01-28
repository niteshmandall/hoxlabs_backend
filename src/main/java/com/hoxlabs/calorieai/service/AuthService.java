package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.UpdateProfileRequest;
import com.hoxlabs.calorieai.dto.UserProfileDTO;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthService {

        private final UserRepository userRepository;

        @org.springframework.cache.annotation.CacheEvict(value = "userProfile", key = "#email")
        public String updateProfilePhoto(String email, String photoUrl) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setProfilePhotoUrl(photoUrl);
                userRepository.save(user);
                return photoUrl;
        }

        public User syncUser(String email, String firebaseUid, com.hoxlabs.calorieai.dto.SyncUserRequest request) {
                return userRepository.findByEmail(email)
                                .map(existingUser -> {
                                        if (existingUser.getFirebaseUid() == null) {
                                                existingUser.setFirebaseUid(firebaseUid);
                                                userRepository.save(existingUser);
                                        }
                                        return existingUser;
                                })
                                .orElseGet(() -> {
                                        // Create new user
                                        User newUser = new User(email, com.hoxlabs.calorieai.entity.Role.USER,
                                                        request.getDailyCalorieGoal(), firebaseUid);

                                        if (request.getName() != null)
                                                newUser.setName(request.getName());
                                        if (request.getAge() != null)
                                                newUser.setAge(request.getAge());
                                        if (request.getGender() != null)
                                                newUser.setGender(request.getGender());
                                        if (request.getWeight() != null)
                                                newUser.setWeight(request.getWeight());
                                        if (request.getHeight() != null)
                                                newUser.setHeight(request.getHeight());
                                        if (request.getFitnessGoal() != null)
                                                newUser.setFitnessGoal(request.getFitnessGoal());
                                        if (request.getProfilePhotoUrl() != null)
                                                newUser.setProfilePhotoUrl(request.getProfilePhotoUrl());

                                        return userRepository.save(newUser);
                                });
        }

        @org.springframework.cache.annotation.Cacheable(value = "userProfile", key = "#email")
        public UserProfileDTO getUserProfile(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

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

        public UserProfileDTO updateProfile(String email, com.hoxlabs.calorieai.dto.UpdateProfileRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (request.getName() != null)
                        user.setName(request.getName());
                if (request.getAge() != null)
                        user.setAge(request.getAge());
                if (request.getGender() != null)
                        user.setGender(request.getGender());
                if (request.getWeight() != null)
                        user.setWeight(request.getWeight());
                if (request.getHeight() != null)
                        user.setHeight(request.getHeight());
                if (request.getFitnessGoal() != null)
                        user.setFitnessGoal(request.getFitnessGoal());
                if (request.getDailyCalorieGoal() != null)
                        user.setCalorieGoal(request.getDailyCalorieGoal());
                if (request.getProteinGoal() != null)
                        user.setProteinGoal(request.getProteinGoal());
                if (request.getCarbsGoal() != null)
                        user.setCarbsGoal(request.getCarbsGoal());
                if (request.getFatGoal() != null)
                        user.setFatGoal(request.getFatGoal());

                userRepository.save(user);

                return getUserProfile(email);
        }
}
