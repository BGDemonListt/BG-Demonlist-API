package com.bgdl.bgdl.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DemonBaseDTO {
    private UUID id;
    private String levelTitle;
    private long levelId;
}
