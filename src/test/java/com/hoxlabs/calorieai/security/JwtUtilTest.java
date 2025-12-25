package com.hoxlabs.calorieai.security;

import com.hoxlabs.calorieai.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;
    // A simplified base64 secret key (just "secret" encoded or sufficient length string)
    // 256-bit key requirement for HS256: 32 bytes.
    // "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=" is 32 chars? No.
    // Let's use a known long secret.
    private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"; 
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", EXPIRATION);

        user = new User();
        user.setEmail("test@example.com");
    }

    @Test
    void generateToken_ShouldReturnNonEmptyString() {
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(user);
        String username = jwtUtil.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(user);
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        assertFalse(jwtUtil.isTokenValid(token, otherUser));
    }

    @Test
    void extractClaim_ShouldRetrieveCustomClaims() {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtUtil.generateToken(claims, user);
        
        Claims extractedClaims = jwtUtil.extractClaim(token, c -> c);
        assertEquals("ADMIN", extractedClaims.get("role"));
    }
    
    @Test
    void isTokenExpired_ShouldReturnFalse_ForNewToken() {
        String token = jwtUtil.generateToken(user);
        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);
        assertTrue(expiration.after(new Date()));
    }
}
