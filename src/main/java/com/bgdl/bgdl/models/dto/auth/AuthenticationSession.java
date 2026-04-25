package com.bgdl.bgdl.models.dto.auth;

import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Internal authentication result that carries freshly issued tokens and the public user payload.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationSession implements Serializable {
    private String accessToken;
    private String refreshToken;
    private PublicUserResponse user;

    public AuthenticationResponse toResponse() {
        return AuthenticationResponse.builder()
                .user(user)
                .build();
    }
}
