package com.bgdl.bgdl.controllers.auth;

import com.bgdl.bgdl.config.FrontendConfig;
import com.bgdl.bgdl.models.request.auth.AuthenticationRequest;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.security.filter.JwtAuthenticationFilter;
import com.bgdl.bgdl.services.auth.AuthenticationService;
import com.bgdl.bgdl.services.impl.auth.events.OnPasswordResetRequestEvent;
import com.bgdl.bgdl.services.impl.auth.events.OnRegistrationCompleteEvent;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controller class for handling authentication-related operations.
 * JWT (access and refresh token);
 * OAuth2;
 * Email confirmation;
 * Forgotten password.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final FrontendConfig frontendConfig;

    @PostMapping("/register")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.register(request);

        return ResponseEntity.ok(authenticationResponse);
    }

    //Endpoint for email confirmation during registration
    @GetMapping("/registrationConfirm")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token, HttpServletResponse httpServletResponse) throws IOException {
        authenticationService.confirmRegistration(token);
        httpServletResponse.sendRedirect(frontendConfig.getLoginUrl());
        return ResponseEntity.ok("User registration confirmed successfully!");
    }

    @PostMapping("/authenticate") // login
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse servletResponse) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    @GetMapping("/refresh-token/{refreshToken}")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> refreshToken(@PathVariable String refreshToken) throws IOException {
        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(authenticationResponse);
    }

    @GetMapping("/me") // Retrieves current user information.
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> getMe(HttpServletRequest request) {
        String jwtToken = (String) request.getAttribute(JwtAuthenticationFilter.JWT_KEY);
        AuthenticationResponse authenticationResponse = authenticationService.me(jwtToken);

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/forgot-password") // Sends link to email so the user can change their password
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        User user = authenticationService.forgotPassword(email);
        return ResponseEntity.ok("Password reset link sent to your email!");
    }

    @PostMapping("/password-reset")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }
}
