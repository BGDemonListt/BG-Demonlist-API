package com.bgdl.bgdl.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application.oauth2.discord")
public class DiscordOAuth2Properties {
    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String authorizationUri;

    @NotBlank
    private String tokenUri;

    @NotBlank
    private String apiBaseUri;

    @NotBlank
    private String userInfoPath;

    @NotNull
    private Duration stateTtl = Duration.ofMinutes(10);
}
