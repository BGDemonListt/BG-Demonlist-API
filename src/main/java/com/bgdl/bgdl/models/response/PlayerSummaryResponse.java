package com.bgdl.bgdl.models.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerSummaryResponse {
    private UUID id;
    private String name;
    private Double points;
    private Integer position;
}
