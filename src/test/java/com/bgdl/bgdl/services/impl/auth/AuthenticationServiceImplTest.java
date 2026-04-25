package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.config.rateLimiting.RateLimiterConfigProperties;
import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.TokenType;
import com.bgdl.bgdl.exceptions.email.EmailNotVerified;
import com.bgdl.bgdl.exceptions.token.ExpiredTokenException;
import com.bgdl.bgdl.exceptions.token.InvalidTokenException;
import com.bgdl.bgdl.exceptions.user.UserLoginException;
import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.entity.Token;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.entity.VerificationToken;
import com.bgdl.bgdl.models.request.auth.AuthenticationRequest;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.repositories.VerificationTokenRepository;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.UserService;
import com.bgdl.bgdl.services.auth.JwtService;
import com.bgdl.bgdl.services.auth.TokenService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PlayerService playerService;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RateLimiterConfigProperties rateLimiterConfigProperties;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticateThrowsUserLoginExceptionWhenEmailDoesNotExist() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("missing@example.com")
                .password("secret")
                .build();

        when(userService.findByEmail(request.getEmail())).thenThrow(new UserNotFoundException());

        assertThrows(UserLoginException.class, () -> authenticationService.authenticate(request));

        verify(userService).findByEmail(request.getEmail());
        verifyNoInteractions(authenticationManager, tokenService);
    }

    @Test
    void authenticateThrowsEmailNotVerifiedWhenAuthenticationManagerRejectsDisabledAccount() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("secret")
                .build();
        User user = user("user@example.com");

        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        assertThrows(EmailNotVerified.class, () -> authenticationService.authenticate(request));

        verify(tokenService, never()).revokeAllUserTokens(any());
        verify(tokenService, never()).generateAuthenticationSession(any());
    }

    @Test
    void authenticateRevokesExistingTokensAndReturnsFreshAuthResponse() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("secret")
                .build();
        User user = user("user@example.com");
        AuthenticationSession response = AuthenticationSession.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(tokenService.generateAuthenticationSession(user)).thenReturn(response);

        AuthenticationSession result = authenticationService.authenticate(request);

        assertSame(response, result);
        verify(tokenService).revokeAllUserTokens(user);
        verify(tokenService).generateAuthenticationSession(user);
    }

    @Test
    void refreshTokenRejectsStoredAccessTokens() {
        String refreshToken = "not-a-refresh-token";
        Token storedToken = token(refreshToken, TokenType.ACCESS, user("user@example.com"));

        when(jwtService.extractUsername(refreshToken)).thenReturn("user@example.com");
        when(tokenService.findByToken(refreshToken)).thenReturn(storedToken);

        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(refreshToken));

        verify(userService, never()).findByEmail(any());
    }

    @Test
    void refreshTokenRevokesStoredRefreshTokenWhenJwtValidationFails() {
        String refreshToken = "refresh-token";
        User user = user("user@example.com");
        Token storedToken = token(refreshToken, TokenType.REFRESH, user);

        when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());
        when(tokenService.findByToken(refreshToken)).thenReturn(storedToken);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(refreshToken));

        verify(tokenService).revokeToken(storedToken);
        verify(tokenService, never()).saveToken(any(), any(), any());
    }

    @Test
    void refreshTokenReturnsNewAccessTokenAndPersistsTokenPair() {
        String refreshToken = "refresh-token";
        User user = user("user@example.com");
        Token storedToken = token(refreshToken, TokenType.REFRESH, user);
        AuthenticationSession response = AuthenticationSession.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());
        when(tokenService.findByToken(refreshToken)).thenReturn(storedToken);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(tokenService.generateAuthenticationSession(user)).thenReturn(response);

        AuthenticationSession result = authenticationService.refreshToken(refreshToken);

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(tokenService).revokeAllUserTokens(user);
        verify(tokenService).generateAuthenticationSession(user);
    }

    @Test
    void meRevokesAllUserTokensWhenAccessTokenCannotBeValidated() {
        String jwtToken = "access-token";
        User user = user("user@example.com");
        Token accessToken = token(jwtToken, TokenType.ACCESS, user);

        when(tokenService.findByToken(jwtToken)).thenReturn(accessToken);
        when(jwtService.isTokenValid(jwtToken, user)).thenThrow(new JwtException("invalid"));

        assertThrows(InvalidTokenException.class, () -> authenticationService.me(jwtToken));

        verify(tokenService).revokeAllUserTokens(user);
        verify(tokenService, never()).findByUser(any());
    }

    @Test
    void meReturnsCurrentUserWithoutReissuingTokens() {
        String jwtToken = "access-token";
        User user = user("user@example.com");
        Token accessToken = token(jwtToken, TokenType.ACCESS, user);
        PublicUserResponse publicUserResponse = PublicUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();

        when(tokenService.findByToken(jwtToken)).thenReturn(accessToken);
        when(jwtService.isTokenValid(jwtToken, user)).thenReturn(true);
        when(userService.toPublicUserResponse(user)).thenReturn(publicUserResponse);

        AuthenticationResponse response = authenticationService.me(jwtToken);

        assertSame(publicUserResponse, response.getUser());
        verify(tokenService, never()).findByUser(any());
        verify(tokenService, never()).saveToken(any(), any(), any());
    }

    @Test
    void resetPasswordEncodesAndPersistsNewPasswordThenDeletesVerificationToken() {
        String resetToken = "reset-token";
        User user = user("user@example.com");
        user.setPassword("old-password");
        VerificationToken verificationToken = verificationToken(resetToken, user, new Date(System.currentTimeMillis() + 60_000L));

        when(verificationTokenRepository.findByToken(resetToken)).thenReturn(verificationToken);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        authenticationService.resetPassword(resetToken, "new-password");

        assertEquals("encoded-password", user.getPassword());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void confirmRegistrationEnablesUserCreatesPlayerAndDeletesVerificationToken() {
        String confirmationToken = "confirm-token";
        User user = user("user@example.com");
        user.setEnabled(false);
        VerificationToken verificationToken =
                verificationToken(confirmationToken, user, new Date(System.currentTimeMillis() + 60_000L));
        User savedUser = user("user@example.com");
        savedUser.setId(user.getId());
        savedUser.setEnabled(true);

        when(verificationTokenRepository.findByToken(confirmationToken)).thenReturn(verificationToken);
        when(userRepository.save(user)).thenReturn(savedUser);

        authenticationService.confirmRegistration(confirmationToken);

        assertEquals(true, user.isEnabled());
        verify(userRepository).save(user);
        verify(playerService).createPlayer(savedUser);
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void confirmRegistrationRejectsExpiredVerificationToken() {
        String confirmationToken = "expired-token";
        VerificationToken verificationToken =
                verificationToken(confirmationToken, user("user@example.com"), new Date(System.currentTimeMillis() - 60_000L));

        when(verificationTokenRepository.findByToken(confirmationToken)).thenReturn(verificationToken);

        assertThrows(ExpiredTokenException.class, () -> authenticationService.confirmRegistration(confirmationToken));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(playerService);
        verify(verificationTokenRepository, never()).delete(any());
    }

    private User user(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setName("BGDL User");
        user.setProvider(Provider.LOCAL);
        return user;
    }

    private Token token(String value, TokenType tokenType, User user) {
        Token token = new Token();
        token.setToken(value);
        token.setTokenType(tokenType);
        token.setUser(user);
        return token;
    }

    private VerificationToken verificationToken(String tokenValue, User user, Date expiryDate) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(tokenValue);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiryDate);
        verificationToken.setCreatedAt(LocalDateTime.now().minusDays(1));
        return verificationToken;
    }
}
