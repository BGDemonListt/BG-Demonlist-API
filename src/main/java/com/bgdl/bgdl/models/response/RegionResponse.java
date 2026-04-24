package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.enums.BulgarianRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionResponse {
    private String code;
    private String name;
    private String flagPath;

    public static RegionResponse from(BulgarianRegion region) {
        return new RegionResponse(region.name(), region.getDisplayName(), region.getFlagPath());
    }
}
