package com.hoxlabs.calorieai.service;

import com.hoxlabs.calorieai.dto.AuthenticationRequest;
import com.hoxlabs.calorieai.dto.AuthenticationResponse;
import com.hoxlabs.calorieai.dto.RegisterRequest;
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
        return AuthenticationResponse.builder()
                .token(jwtToken)
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
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
