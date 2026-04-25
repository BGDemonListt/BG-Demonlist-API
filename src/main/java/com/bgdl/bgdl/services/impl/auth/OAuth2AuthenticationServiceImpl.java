package com.bgdl.bgdl.services.impl.auth;

import com.bgdl.bgdl.config.DiscordOAuth2Properties;
import com.bgdl.bgdl.config.FrontendConfig;
import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.exceptions.token.InvalidTokenException;
import com.bgdl.bgdl.exceptions.user.InvalidDiscordAuthorizationException;
import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.dto.auth.DiscordTokenResponse;
import com.bgdl.bgdl.models.dto.auth.DiscordUserInfoDTO;
import com.bgdl.bgdl.models.dto.auth.OAuth2UserInfoDTO;
import com.bgdl.bgdl.models.entity.DiscordProfile;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.services.auth.OAuth2AuthenticationService;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.auth.TokenService;
import com.bgdl.bgdl.services.UserService;
import com.bgdl.bgdl.services.auth.DiscordLinkStateService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Handles third-party OAuth flows used by the application.
 */
@Service
@RequiredArgsConstructor
public class OAuth2AuthenticationServiceImpl implements OAuth2AuthenticationService {
    private static final String DISCORD_CDN_BASE_URL = "https://cdn.discordapp.com";
    private static final List<String> GOOGLE_SCOPES = List.of("email", "profile", "openid");
    private static final List<String> DISCORD_SCOPES = List.of("identify");

    private final FrontendConfig frontendConfig;
    private final WebClient userInfoClient;
    private final UserService userService;
    private final PlayerService playerService;
    private final TokenService tokenService;
    private final DiscordOAuth2Properties discordOAuth2Properties;
    private final DiscordLinkStateService discordLinkStateService;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String clientSecret;

    @Override
    public String getOAuthGoogleLoginUrl() {
        return new GoogleAuthorizationCodeRequestUrl(clientId, frontendConfig.getOauth2RedirectUrl(), GOOGLE_SCOPES).build();
    }

    @Override
    public AuthenticationSession processOAuthGoogleLogin(String code) {
        String token = authorizeWithGoogle(code);
        OAuth2UserInfoDTO oAuth2UserInfoDTO = getUserInfoFromGoogleToken(token);
        oAuth2UserInfoDTO.setProvider(Provider.GOOGLE);

        User user = userService.processOAuthUser(oAuth2UserInfoDTO);
        playerService.createPlayer(user);
        tokenService.revokeAllUserTokens(user);

        userService.enableUser(user);
        return tokenService.generateAuthenticationSession(user);
    }

    @Override
    public String getDiscordLinkUrl(UUID userId) {
        return UriComponentsBuilder
                .fromUriString(discordOAuth2Properties.getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", discordOAuth2Properties.getClientId())
                .queryParam("scope", String.join(" ", DISCORD_SCOPES))
                .queryParam("redirect_uri", frontendConfig.getDiscordLinkRedirectUrl())
                .queryParam("state", discordLinkStateService.generateState(userId))
                .build(true)
                .toUriString();
    }

    @Override
    public PublicUserResponse linkDiscordAccount(UUID userId, String code, String state) {
        discordLinkStateService.validateState(state, userId);

        String discordAccessToken = authorizeWithDiscord(code);
        DiscordUserInfoDTO discordUserInfo = getDiscordUserInfo(discordAccessToken);
        DiscordProfile discordProfile = buildDiscordProfile(discordUserInfo);

        return userService.linkDiscordAccount(userId, discordProfile);
    }

    @Override
    public PublicUserResponse unlinkDiscordAccount(UUID userId) {
        return userService.unlinkDiscordAccount(userId);
    }

    private OAuth2UserInfoDTO getUserInfoFromGoogleToken(String googleToken) {
        return userInfoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth2/v3/userinfo")
                        .queryParam("access_token", googleToken)
                        .build())
                .retrieve()
                .bodyToMono(OAuth2UserInfoDTO.class)
                .block();
    }

    private String authorizeWithGoogle(String code) {
        try {
            return new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    clientId,
                    clientSecret,
                    code,
                    frontendConfig.getOauth2RedirectUrl())
                    .execute()
                    .getAccessToken();
        } catch (IOException exception) {
            throw new InvalidTokenException();
        }
    }

    private String authorizeWithDiscord(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", discordOAuth2Properties.getClientId());
        requestBody.add("client_secret", discordOAuth2Properties.getClientSecret());
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", frontendConfig.getDiscordLinkRedirectUrl());

        try {
            DiscordTokenResponse response = WebClient.builder()
                    .build()
                    .post()
                    .uri(discordOAuth2Properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(requestBody))
                    .retrieve()
                    .bodyToMono(DiscordTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
                throw new InvalidDiscordAuthorizationException();
            }

            return response.getAccessToken();
        } catch (WebClientResponseException exception) {
            throw new InvalidDiscordAuthorizationException();
        }
    }

    private DiscordUserInfoDTO getDiscordUserInfo(String accessToken) {
        try {
            DiscordUserInfoDTO response = WebClient.builder()
                    .baseUrl(discordOAuth2Properties.getApiBaseUri())
                    .build()
                    .get()
                    .uri(discordOAuth2Properties.getUserInfoPath())
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(DiscordUserInfoDTO.class)
                    .block();

            if (response == null || response.getId() == null || response.getUsername() == null) {
                throw new InvalidDiscordAuthorizationException();
            }

            return response;
        } catch (WebClientResponseException exception) {
            throw new InvalidDiscordAuthorizationException();
        }
    }

    private DiscordProfile buildDiscordProfile(DiscordUserInfoDTO discordUserInfo) {
        return DiscordProfile.builder()
                .id(discordUserInfo.getId())
                .username(discordUserInfo.getUsername())
                .avatarUrl(buildDiscordAvatarUrl(discordUserInfo.getId(), discordUserInfo.getAvatar()))
                .linkedAt(LocalDateTime.now())
                .build();
    }

    private String buildDiscordAvatarUrl(String userId, String avatarHash) {
        if (avatarHash == null || avatarHash.isBlank()) {
            long parsedUserId = Long.parseUnsignedLong(userId);
            long avatarIndex = (parsedUserId >>> 22) % 6;
            return String.format("%s/embed/avatars/%d.png", DISCORD_CDN_BASE_URL, avatarIndex);
        }

        StringBuilder avatarUrl = new StringBuilder(
                String.format("%s/avatars/%s/%s.webp?size=256", DISCORD_CDN_BASE_URL, userId, avatarHash)
        );

        if (avatarHash.startsWith("a_")) {
            avatarUrl.append("&animated=true");
        }

        return avatarUrl.toString();
    }
}
