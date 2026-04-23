package com.bgdl.bgdl.services.impl.security;

import com.bgdl.bgdl.enums.TokenType;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.Token;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.AuthenticationResponse;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.repositories.TokenRepository;
import com.bgdl.bgdl.repositories.VerificationTokenRepository;
import com.bgdl.bgdl.services.JwtService;
import com.bgdl.bgdl.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void generateAuthResponseCopiesPlayerIdIntoPublicUserResponse() {
        UUID userId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Player player = new Player();
        player.setId(playerId);

        User user = new User();
        user.setId(userId);
        user.setPlayer(player);

        PublicUserResponse publicUserResponse = new PublicUserResponse();

        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(userService.toPublicUserResponse(user)).thenReturn(publicUserResponse);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationResponse response = tokenService.generateAuthResponse(user);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(publicUserResponse, response.getUser());
        verify(tokenRepository, times(2)).save(any(Token.class));
        verify(userService).toPublicUserResponse(user);
    }
}
