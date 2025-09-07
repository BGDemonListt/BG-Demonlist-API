package com.bgdl.bgdl.models.dto.response;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.models.dto.common.DemonDTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DemonResponseDTO extends DemonDTO {
}
