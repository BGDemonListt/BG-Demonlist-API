package com.bgdl.bgdl.models.dto.common;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemonDTO {
    private String levelId;
    private String creatorName;
    private String creatorId;
    private String description;
    private String levelPassword;
    private String musicName;
    private String musicId;
    private String musicCreatorName;
    private String musicUrl;
    private int position;
    private int points;
    private DemonDifficulty difficulty;
}
