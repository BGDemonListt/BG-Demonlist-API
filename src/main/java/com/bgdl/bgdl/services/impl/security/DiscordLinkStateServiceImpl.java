package com.bgdl.bgdl.services.impl.security;

import com.bgdl.bgdl.config.DiscordOAuth2Properties;
import com.bgdl.bgdl.exceptions.user.InvalidDiscordLinkStateException;
import com.bgdl.bgdl.services.DiscordLinkStateService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class DiscordLinkStateServiceImpl implements DiscordLinkStateService {
    private static final String PURPOSE_CLAIM = "purpose";
    private static final String DISCORD_LINK_PURPOSE = "discord-link";

    private final DiscordOAuth2Properties discordOAuth2Properties;
    private final String secretKey;

    public DiscordLinkStateServiceImpl(
            DiscordOAuth2Properties discordOAuth2Properties,
            @Value("${spring.security.jwt.secret-key}") String secretKey
    ) {
        this.discordOAuth2Properties = discordOAuth2Properties;
        this.secretKey = secretKey;
    }

    @Override
    public String generateState(UUID userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(PURPOSE_CLAIM, DISCORD_LINK_PURPOSE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(discordOAuth2Properties.getStateTtl())))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public void validateState(String state, UUID expectedUserId) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(state)
                    .getBody();

            UUID userIdFromState = UUID.fromString(claims.getSubject());
            String purpose = claims.get(PURPOSE_CLAIM, String.class);

            if (!DISCORD_LINK_PURPOSE.equals(purpose) || !expectedUserId.equals(userIdFromState)) {
                throw new InvalidDiscordLinkStateException();
            }
        } catch (IllegalArgumentException | JwtException exception) {
            throw new InvalidDiscordLinkStateException();
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
