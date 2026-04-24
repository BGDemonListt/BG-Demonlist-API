package com.bgdl.bgdl.models.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SkillsetTagResponse {
    private UUID id;
    private String name;
}
