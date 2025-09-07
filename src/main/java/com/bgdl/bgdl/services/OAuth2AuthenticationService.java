package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.dto.auth.AuthenticationResponse;

public interface OAuth2AuthenticationService {

    String getOAuthGoogleLoginUrl();

    AuthenticationResponse processOAuthGoogleLogin(String code);
}
