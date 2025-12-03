package com.bgdl.bgdl.models.dto.response;

import com.bgdl.bgdl.models.dto.auth.PublicUserDTO;
import com.bgdl.bgdl.models.dto.common.DemonDTO;
import com.bgdl.bgdl.models.dto.common.RecordSubmissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionResponseDTO extends RecordSubmissionDTO {
    private PublicUserDTO userId;
    private DemonDTO demonId;
}
