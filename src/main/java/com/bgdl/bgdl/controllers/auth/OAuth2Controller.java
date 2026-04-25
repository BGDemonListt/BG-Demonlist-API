package com.bgdl.bgdl.controllers.auth;

import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.security.cookies.AuthCookieService;
import com.bgdl.bgdl.security.filter.JwtAuthenticationFilter;
import com.bgdl.bgdl.services.auth.OAuth2AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {
    private final OAuth2AuthenticationService oAuth2AuthenticationService;
    private final AuthCookieService authCookieService;

    @GetMapping("/url/google")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> auth() {
        return ResponseEntity.ok(oAuth2AuthenticationService.getOAuthGoogleLoginUrl());
    }

    @GetMapping("/authenticate/google")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> googleAuthenticate(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        AuthenticationSession authenticationSession = oAuth2AuthenticationService.processOAuthGoogleLogin(code);
        authCookieService.addAuthenticationCookies(response, authenticationSession);
        return ResponseEntity.ok(authenticationSession.toResponse());
    }

    @GetMapping("/url/discord/link")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> getDiscordLinkUrl(HttpServletRequest request) {
        PublicUserResponse user = (PublicUserResponse) request.getAttribute(JwtAuthenticationFilter.USER_KEY);
        return ResponseEntity.ok(oAuth2AuthenticationService.getDiscordLinkUrl(user.getId()));
    }

    @PostMapping("/link/discord")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<PublicUserResponse> linkDiscordAccount(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest request
    ) {
        PublicUserResponse user = (PublicUserResponse) request.getAttribute(JwtAuthenticationFilter.USER_KEY);
        return ResponseEntity.ok(oAuth2AuthenticationService.linkDiscordAccount(user.getId(), code, state));
    }

    @DeleteMapping("/link/discord")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<PublicUserResponse> unlinkDiscordAccount(HttpServletRequest request) {
        PublicUserResponse user = (PublicUserResponse) request.getAttribute(JwtAuthenticationFilter.USER_KEY);
        return ResponseEntity.ok(oAuth2AuthenticationService.unlinkDiscordAccount(user.getId()));
    }
}
