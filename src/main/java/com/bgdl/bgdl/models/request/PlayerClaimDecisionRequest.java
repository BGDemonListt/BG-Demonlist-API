package com.bgdl.bgdl.models.request;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerClaimDecisionRequest {
    @NotNull
    private PlayerClaimStatus status;
}
