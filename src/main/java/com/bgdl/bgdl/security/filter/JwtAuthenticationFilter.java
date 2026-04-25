package com.bgdl.bgdl.security.filter;

import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.TokenRepository;
import com.bgdl.bgdl.security.cookies.AuthCookieService;
import com.bgdl.bgdl.services.auth.JwtService;
import com.bgdl.bgdl.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * Filter responsible for JWT-based authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Key to retrieve user information from request attribute.
     */
    public static final String USER_KEY = "user";
    public static final String JWT_KEY = "jwt";

    private final AuthCookieService authCookieService;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        request.setAttribute(USER_KEY, null);
        request.setAttribute(JWT_KEY, null);

        final String jwt = authCookieService.extractAccessToken(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception exception) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;

            try {
                userDetails = userService.findByEmail(userEmail);
            } catch (UserNotFoundException exception) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if token is valid and not revoked or expired
            boolean isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {

                // Set user authentication in security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                if (userDetails instanceof User user) {
                    request.setAttribute(USER_KEY, userService.toPublicUserResponse(user));
                }
                request.setAttribute(JWT_KEY, jwt);
            }
        }

        filterChain.doFilter(request, response);
    }
}
