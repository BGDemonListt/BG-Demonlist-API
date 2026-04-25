package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.enums.TokenType;
import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.entity.Token;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.entity.VerificationToken;
import com.bgdl.bgdl.repositories.TokenRepository;
import com.bgdl.bgdl.repositories.VerificationTokenRepository;
import com.bgdl.bgdl.services.auth.JwtService;
import com.bgdl.bgdl.services.auth.TokenService;
import com.bgdl.bgdl.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation responsible for handling authentication tokens.
 */
@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public Token findByToken(String jwt) {
        return tokenRepository.findByToken(jwt).orElse(null);
    }

    @Override
    public List<Token> findByUser(User user) {
        return tokenRepository.findAllByUser(user);
    }

    @Override
    public void saveToken(User user, String jwtToken, TokenType tokenType) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public void revokeToken(Token token) {
        tokenRepository.delete(token);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        tokenRepository.deleteAll(tokenRepository.findAllByUser(user));
    }

    @Override
    @Transactional
    public void logoutToken(String jwt) {
        Token storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);

        if (storedToken == null) {
            return;
        }

        revokeAllUserTokens(storedToken.getUser());
        SecurityContextHolder.clearContext();
    }

    @Override
    public AuthenticationSession generateAuthenticationSession(User user) {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        PublicUserResponse publicUser = userService.toPublicUserResponse(user);

        saveToken(user, jwtToken, TokenType.ACCESS);
        saveToken(user, refreshToken, TokenType.REFRESH);

        return AuthenticationSession
                .builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(publicUser)
                .build();
    }

    @Override
    @Transactional
    public void createVerificationToken(User user, String token) {
        clearVerificationTokensByUser(user);
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }

    @Override
    public void clearVerificationTokensByUser(User user) {
        verificationTokenRepository.deleteAllByUser(user);
    }

}
