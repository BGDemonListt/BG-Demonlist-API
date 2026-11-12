package com.bgdl.bgdl.models.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayerClaimRequestCreate {
    @NotNull
    private UUID playerId;
}
