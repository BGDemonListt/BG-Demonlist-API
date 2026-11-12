package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.response.PlayerClaimRequestResponse;

import java.util.List;
import java.util.UUID;

public interface PlayerClaimService {
    PlayerClaimRequestResponse requestClaim(UUID requesterId, UUID playerId);
    List<PlayerClaimRequestResponse> getRequests();
    PlayerClaimRequestResponse decide(UUID requestId, UUID adminId, boolean accept);
}
