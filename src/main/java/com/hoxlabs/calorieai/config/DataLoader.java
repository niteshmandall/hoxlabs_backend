package com.hoxlabs.calorieai.config;

import com.hoxlabs.calorieai.entity.Role;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner load(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                // Email, Role, CalorieGoal, FirebaseUid
                User admin = new User("admin@hoxlabs.com", Role.ADMIN, 2000, "admin-firebase-uid-placeholder");
                userRepository.save(admin);
                System.out.println("Seeded admin user -> email=admin@hoxlabs.com");
            }
        };
    }
}
