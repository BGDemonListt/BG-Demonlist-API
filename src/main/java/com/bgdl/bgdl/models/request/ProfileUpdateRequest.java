package com.bgdl.bgdl.models.request;

import com.bgdl.bgdl.enums.BulgarianRegion;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @Size(min = 2, max = 255, message = "Името трябва да е между 2 и 255 символа!")
    private String name;

    private BulgarianRegion region;
}
