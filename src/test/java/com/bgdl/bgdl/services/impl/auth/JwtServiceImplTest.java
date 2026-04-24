package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.models.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceImplTest {

    private static final String SECRET_KEY = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

    private final JwtServiceImpl jwtService = new JwtServiceImpl(SECRET_KEY, 60_000L, 300_000L);

    @Test
    void generateTokenPreservesSubjectAndCustomClaims() {
        User user = user("user@example.com");

        String token = jwtService.generateToken(Map.of("scope", "admin"), user);

        assertEquals("user@example.com", jwtService.extractUsername(token));
        assertEquals("admin", jwtService.extractClaim(token, claims -> claims.get("scope", String.class)));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        String token = jwtService.generateToken(user("first@example.com"));

        assertFalse(jwtService.isTokenValid(token, user("second@example.com")));
    }

    @Test
    void generateRefreshTokenUsesLongerExpirationWindowThanAccessTokens() {
        User user = user("user@example.com");

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Claims accessClaims = jwtService.extractAllClaims(accessToken);
        Claims refreshClaims = jwtService.extractAllClaims(refreshToken);

        assertTrue(refreshClaims.getExpiration().after(accessClaims.getExpiration()));
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setProvider(Provider.LOCAL);
        return user;
    }
}
