package com.hoxlabs.config;

import com.hoxlabs.model.Role;
import com.hoxlabs.model.User;
import com.hoxlabs.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner load(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if(userRepository.count() == 0 ) {
                User admin = new User("amdin", "admin.hoxlabs.com", passwordEncoder.encode("admin123"), Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Seeded admin user -> username=admin, password=admin234");
            }
        };
    }
}
