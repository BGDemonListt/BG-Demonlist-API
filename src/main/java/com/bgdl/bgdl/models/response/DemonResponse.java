package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DemonResponse extends DemonDTO {
    private UUID id;
}
