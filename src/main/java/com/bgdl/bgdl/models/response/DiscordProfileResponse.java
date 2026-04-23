package com.bgdl.bgdl.models.response;

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
public class DiscordProfileResponse {
    private String id;
    private String username;
    private String avatarUrl;
    private LocalDateTime linkedAt;
}
