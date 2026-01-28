package com.hoxlabs.calorieai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uc_users_email", columnNames = "email")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String firebaseUid;

    @Column(nullable = false)
    private Integer calorieGoal = 2000; // Default goal

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    private String name;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Float weight;
    private Float height;

    @Enumerated(EnumType.STRING)
    private FitnessGoal fitnessGoal;

    private String profilePhotoUrl;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User(String email, Role role, Integer calorieGoal, String firebaseUid) {
        this.email = email;
        this.role = role == null ? Role.USER : role;
        this.calorieGoal = calorieGoal == null ? 2000 : calorieGoal;
        this.firebaseUid = firebaseUid;
        this.createdAt = Instant.now();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
