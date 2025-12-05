package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import lombok.Data;

import java.util.List;

@Data
public class PlayerResponse {
    private String name;
    private Double points;
    private Integer rank;
    private boolean banned;
    private DemonBaseDTO hardestDemon;
    private List<DemonBaseDTO> completedDemons;
}
