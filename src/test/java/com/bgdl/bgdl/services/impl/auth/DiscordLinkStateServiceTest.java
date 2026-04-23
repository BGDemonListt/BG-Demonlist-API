package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.config.DiscordOAuth2Properties;
import com.bgdl.bgdl.exceptions.user.InvalidDiscordLinkStateException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscordLinkStateServiceTest {

    private final DiscordLinkStateServiceImpl discordLinkStateService = new DiscordLinkStateServiceImpl(
            buildDiscordOAuth2Properties(),
            Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8))
    );

    @Test
    void validateStateAcceptsTokensCreatedForTheSameUser() {
        UUID userId = UUID.randomUUID();

        String state = discordLinkStateService.generateState(userId);

        assertDoesNotThrow(() -> discordLinkStateService.validateState(state, userId));
    }

    @Test
    void validateStateRejectsTokensCreatedForDifferentUsers() {
        UUID userId = UUID.randomUUID();
        String state = discordLinkStateService.generateState(userId);

        assertThrows(
                InvalidDiscordLinkStateException.class,
                () -> discordLinkStateService.validateState(state, UUID.randomUUID())
        );
    }

    private static DiscordOAuth2Properties buildDiscordOAuth2Properties() {
        DiscordOAuth2Properties properties = new DiscordOAuth2Properties();
        properties.setClientId("discord-client-id");
        properties.setClientSecret("discord-client-secret");
        properties.setAuthorizationUri("https://discord.com/oauth2/authorize");
        properties.setTokenUri("https://discord.com/api/oauth2/token");
        properties.setApiBaseUri("https://discord.com/api/v10");
        properties.setUserInfoPath("/users/@me");
        properties.setStateTtl(Duration.ofMinutes(10));
        return properties;
    }
}
