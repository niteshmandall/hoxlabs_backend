package com.hoxlabs.calorieai.config;

import com.hoxlabs.calorieai.entity.Role;
import com.hoxlabs.calorieai.entity.User;
import com.hoxlabs.calorieai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner load(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if(userRepository.count() == 0 ) {
                // Email, Password, Role, CalorieGoal
                User admin = new User("admin@hoxlabs.com", passwordEncoder.encode("admin123"), Role.ADMIN, 2000);
                userRepository.save(admin);
                System.out.println("Seeded admin user -> email=admin@hoxlabs.com");
            }
        };
    }
}
