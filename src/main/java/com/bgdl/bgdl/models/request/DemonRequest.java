package com.bgdl.bgdl.models.request;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DemonRequest {
    private String levelTitle;
    private long levelId;
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

    @Size(max = 4, message = "Един демон може да има най-много 4 skillset тага!")
    private Set<UUID> skillsetTagIds = new LinkedHashSet<>();
}
