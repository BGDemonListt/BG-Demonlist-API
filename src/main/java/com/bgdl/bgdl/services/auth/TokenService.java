package com.bgdl.bgdl.services.auth;

import com.bgdl.bgdl.enums.TokenType;
import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.entity.Token;
import com.bgdl.bgdl.models.entity.User;

import java.util.List;

public interface TokenService {
    Token findByToken(String jwt);

    List<Token> findByUser(User user);

    void saveToken(User user, String jwtToken, TokenType tokenType);

    void revokeToken(Token token);

    void revokeAllUserTokens(User user);

    void logoutToken(String jwt);

    AuthenticationSession generateAuthenticationSession(User user);

    void createVerificationToken(User user, String token);

    void clearVerificationTokensByUser(User user);
}
