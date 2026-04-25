package com.bgdl.bgdl.services.auth;

import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.response.PublicUserResponse;

import java.util.UUID;

public interface OAuth2AuthenticationService {

    String getOAuthGoogleLoginUrl();

    AuthenticationSession processOAuthGoogleLogin(String code);

    String getDiscordLinkUrl(UUID userId);

    PublicUserResponse linkDiscordAccount(UUID userId, String code, String state);

    PublicUserResponse unlinkDiscordAccount(UUID userId);
}
