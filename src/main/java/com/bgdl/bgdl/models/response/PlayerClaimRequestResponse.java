package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PlayerClaimRequestResponse {
    private UUID id;
    private UUID requesterId;
    private String requesterName;
    private PlayerSummaryResponse player;
    private PlayerClaimStatus status;
    private UUID reviewedById;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
