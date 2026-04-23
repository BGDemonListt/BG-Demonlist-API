package com.bgdl.bgdl.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DiscordProfile {
    @Column(name = "discord_id", unique = true)
    private String id;

    @Column(name = "discord_username")
    private String username;

    @Column(name = "discord_avatar_url")
    private String avatarUrl;

    @Column(name = "discord_linked_at")
    private LocalDateTime linkedAt;
}
