package com.bgdl.bgdl.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SkillsetTagRequest {
    @NotBlank(message = "Името на тага е задължително")
    @Size(max = 50, message = "Името на тага трябва да е до 50 символа")
    private String name;
}
