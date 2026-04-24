package com.bgdl.bgdl.models.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DemonSummaryResponse {
    private UUID id;
    private Long levelId;
    private String name;
    private int position;
    private Double points;
    private String creator;
    private String youtubeUrl;
    private List<SkillsetTagResponse> skillsetTags;
}
