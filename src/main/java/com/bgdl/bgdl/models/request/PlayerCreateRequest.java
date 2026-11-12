package com.bgdl.bgdl.models.request;

import com.bgdl.bgdl.enums.BulgarianRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerCreateRequest {
    @NotBlank
    @Size(min = 2)
    private String name;
    private BulgarianRegion region;
}
