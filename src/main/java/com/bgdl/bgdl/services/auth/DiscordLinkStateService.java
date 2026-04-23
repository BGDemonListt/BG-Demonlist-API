package com.bgdl.bgdl.services.auth;

import java.util.UUID;

public interface DiscordLinkStateService {
    String generateState(UUID userId);

    void validateState(String state, UUID expectedUserId);
}
