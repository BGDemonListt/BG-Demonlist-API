package com.bgdl.bgdl.security;

import com.bgdl.bgdl.services.auth.TokenService;
import com.bgdl.bgdl.security.cookies.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


/**
 * LogoutHandler is responsible for handling user logout by invalidating the JWT token and removing associated cookies.
 */
@Service
@RequiredArgsConstructor
public class LogoutHandler implements org.springframework.security.web.authentication.logout.LogoutHandler {

    private final TokenService tokenService;
    private final AuthCookieService authCookieService;

    /**
     * Performs user logout by invalidating the JWT token and removing associated cookies.
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        authCookieService.clearAuthenticationCookies(response);

        String jwt = authCookieService.extractAccessToken(request);

        if (jwt == null) {
            jwt = authCookieService.extractRefreshToken(request);
        }

        if (jwt != null) {
            tokenService.logoutToken(jwt);
            return;
        }

        SecurityContextHolder.clearContext();
    }
}
