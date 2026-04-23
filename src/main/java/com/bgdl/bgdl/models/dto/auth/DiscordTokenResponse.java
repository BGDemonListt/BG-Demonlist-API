package com.bgdl.bgdl.models.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscordTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
