package com.bgdl.bgdl.security.cookies;

import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Encapsulates authentication cookie creation, clearing, and extraction.
 */
@Service
@RequiredArgsConstructor
public class AuthCookieService {
    private final AuthCookieProperties authCookieProperties;

    @Value("${spring.security.jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${spring.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public void addAuthenticationCookies(HttpServletResponse response, AuthenticationSession authenticationSession) {
        addCookie(
                response,
                authCookieProperties.getAccessToken().getName(),
                authenticationSession.getAccessToken(),
                authCookieProperties.getAccessToken().getPath(),
                Duration.ofMillis(accessTokenExpiration)
        );
        addCookie(
                response,
                authCookieProperties.getRefreshToken().getName(),
                authenticationSession.getRefreshToken(),
                authCookieProperties.getRefreshToken().getPath(),
                Duration.ofMillis(refreshTokenExpiration)
        );
    }

    public void clearAuthenticationCookies(HttpServletResponse response) {
        addCookie(
                response,
                authCookieProperties.getAccessToken().getName(),
                "",
                authCookieProperties.getAccessToken().getPath(),
                Duration.ZERO
        );
        addCookie(
                response,
                authCookieProperties.getRefreshToken().getName(),
                "",
                authCookieProperties.getRefreshToken().getPath(),
                Duration.ZERO
        );
    }

    public String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, authCookieProperties.getAccessToken().getName());
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, authCookieProperties.getRefreshToken().getName());
    }

    private void addCookie(
            HttpServletResponse response,
            String cookieName,
            String value,
            String path,
            Duration maxAge
    ) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, value)
                .httpOnly(authCookieProperties.isHttpOnly())
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(path)
                .maxAge(maxAge);

        if (StringUtils.hasText(authCookieProperties.getDomain())) {
            cookieBuilder.domain(authCookieProperties.getDomain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
