package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlayerResponse {
    private UUID id;
    private String name;
    private Double points;
    private Integer rank;
    private boolean banned;
    private DemonBaseDTO hardestDemon;
    private List<DemonBaseDTO> completedDemons;
}
