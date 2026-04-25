package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.config.rateLimiting.RateLimiterConfigProperties;
import com.bgdl.bgdl.enums.TokenType;
import com.bgdl.bgdl.exceptions.email.EmailNotVerified;
import com.bgdl.bgdl.exceptions.token.ExpiredTokenException;
import com.bgdl.bgdl.exceptions.token.InvalidTokenException;
import com.bgdl.bgdl.exceptions.user.UserLoginException;
import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.request.auth.AuthenticationRequest;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.Token;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.entity.VerificationToken;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.repositories.VerificationTokenRepository;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.UserService;
import com.bgdl.bgdl.services.auth.AuthenticationService;
import com.bgdl.bgdl.services.auth.JwtService;
import com.bgdl.bgdl.services.auth.TokenService;
import com.bgdl.bgdl.services.impl.auth.events.OnPasswordResetRequestEvent;
import com.bgdl.bgdl.services.impl.auth.events.OnRegistrationCompleteEvent;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final ApplicationEventPublisher eventPublisher;
    private final PlayerService playerService;
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterConfigProperties rateLimiterConfigProperties;

    /**
     * Registers a new user based on the provided registration request.
     */
    @Override
    public AuthenticationSession register(RegisterRequest request) {
        User user = userService.createUser(request);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));

        return tokenService.generateAuthenticationSession(user);
    }

    // Login with correct email and password
    @Override
    public AuthenticationSession authenticate(AuthenticationRequest request) {
        User user;

        try {
            user = userService.findByEmail(request.getEmail());
        } catch (UserNotFoundException userNotFoundException) {
            throw new UserLoginException();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException exception) {
            throw new EmailNotVerified();
        } catch (AuthenticationException exception) {
            throw new UserLoginException();
        }

        tokenService.revokeAllUserTokens(user);
        return tokenService.generateAuthenticationSession(user);
    }

    /**
     * Validates the refresh token currently stored in the cookie and rotates the token pair.
     */
    @Override
    public AuthenticationSession refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException();
        }

        String userEmail;

        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (JwtException exception) {
            throw new InvalidTokenException();
        }

        if (userEmail == null) {
            throw new InvalidTokenException();
        }

        Token token = tokenService.findByToken(refreshToken);
        if (token == null || token.getTokenType() != TokenType.REFRESH) {
            throw new InvalidTokenException();
        }

        User user = token.getUser();

        if (user == null || !userEmail.equals(user.getEmail())) {
            throw new InvalidTokenException();
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            tokenService.revokeToken(token);
            throw new InvalidTokenException();
        }

        tokenService.revokeAllUserTokens(user);
        return tokenService.generateAuthenticationSession(user);
    }

    /**
     * Retrieves the current user from the access token carried by the request cookie.
     */
    @Override
    public AuthenticationResponse me(String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new InvalidTokenException();
        }

        Token accessToken = tokenService.findByToken(jwtToken);

        if (accessToken == null || accessToken.getTokenType() != TokenType.ACCESS) {
            throw new InvalidTokenException();
        }

        User user = accessToken.getUser();

        boolean isTokenValid;

        try {
            isTokenValid = jwtService.isTokenValid(accessToken.getToken(), user);
        } catch (JwtException jwtException) {
            isTokenValid = false;
        }

        if (!isTokenValid) {
            tokenService.revokeAllUserTokens(user);
            throw new InvalidTokenException();
        }

        PublicUserResponse publicUser = userService.toPublicUserResponse(user);

        return AuthenticationResponse
                .builder()
                .user(publicUser)
                .build();
    }

    /**
     * Resets the password for a user based on the provided token and new password.
     */
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new InvalidTokenException();
        }


        User user = verificationToken.getUser();
        if (user == null) {
            throw new InvalidTokenException();
        }

        verificationToken.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public void confirmRegistration(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new ExpiredTokenException();
        }

        verificationToken.setCreatedAt(LocalDateTime.now());

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ExpiredTokenException();
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        playerService.createPlayer(savedUser);

        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public User forgotPassword(String email) {
        User user = userService.findByEmail(email);

        if (!user.isEnabled()) {
            throw new EmailNotVerified();
        }

        eventPublisher.publishEvent(new OnPasswordResetRequestEvent(user));
        return user;
    }
}
