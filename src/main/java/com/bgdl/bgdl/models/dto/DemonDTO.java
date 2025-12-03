package com.bgdl.bgdl.models.dto;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemonDTO {
    private String levelTitle;
    private long levelId;
    private String creatorName;
    private long creatorId;
    private String description;
    private String levelPassword;
    private String musicName;
    private long musicId;
    private String musicCreatorName;
    private String musicUrl;
    private int position;
    private int points;
    private DemonDifficulty difficulty;
}
