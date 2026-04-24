package com.bgdl.bgdl.models.dto;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DemonDTO extends DemonBaseDTO{
    private String creatorName;
    private long creatorId;
    private String description;
    private String levelPassword;
    private String youtubeUrl;
    private String musicName;
    private long musicId;
    private String musicCreatorName;
    private String musicUrl;
    private int requirement;
    private int position;
    private Double points;
    private DemonDifficulty difficulty;
    private List<SkillsetTagResponse> skillsetTags;
}
