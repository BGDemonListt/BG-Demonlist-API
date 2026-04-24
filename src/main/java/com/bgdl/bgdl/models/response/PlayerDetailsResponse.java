package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlayerDetailsResponse {
    private UUID id;
    private String name;
    private RegionResponse region;
    private Double points;
    private Integer position;
    private DemonBaseDTO hardestDemon;
    private List<DemonBaseDTO> completedDemons;
}
