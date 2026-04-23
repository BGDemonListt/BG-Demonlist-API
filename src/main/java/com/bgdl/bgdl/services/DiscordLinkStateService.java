package com.bgdl.bgdl.services;

import java.util.UUID;

public interface DiscordLinkStateService {
    String generateState(UUID userId);

    void validateState(String state, UUID expectedUserId);
}
