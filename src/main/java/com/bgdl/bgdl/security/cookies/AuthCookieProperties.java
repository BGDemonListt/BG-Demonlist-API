package com.bgdl.bgdl.security.cookies;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for authentication cookies.
 */
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application.security.cookies")
public class AuthCookieProperties {
    private boolean secure = false;
    private boolean httpOnly = true;
    private String sameSite = "Lax";
    private String domain;
    private CookieSettings accessToken = new CookieSettings("BGDL_ACCESS_TOKEN", "/");
    private CookieSettings refreshToken = new CookieSettings("BGDL_REFRESH_TOKEN", "/api/v1/auth");

    @Data
    public static class CookieSettings {
        private String name;
        private String path;

        public CookieSettings() {
        }

        public CookieSettings(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }
}
