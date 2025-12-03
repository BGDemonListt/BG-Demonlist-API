package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.response.AdminUserResponse;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.handlers.filters.JwtAuthenticationFilter;
import com.bgdl.bgdl.services.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}/admin")
    public ResponseEntity<AdminUserResponse> getByIdAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getByIdAdmin(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<AdminUserResponse> update(@PathVariable("id") UUID id, @RequestBody AdminUserResponse userDTO, HttpServletRequest httpServletRequest) {
        PublicUserResponse user = (PublicUserResponse) httpServletRequest.getAttribute(JwtAuthenticationFilter.USER_KEY);
        return ResponseEntity.ok(userService.updateUser(id, userDTO, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id, HttpServletRequest httpServletRequest) {
        PublicUserResponse user = (PublicUserResponse) httpServletRequest.getAttribute(JwtAuthenticationFilter.USER_KEY);
        userService.deleteUserById(id, user);
        return ResponseEntity.ok().build();
    }
}
