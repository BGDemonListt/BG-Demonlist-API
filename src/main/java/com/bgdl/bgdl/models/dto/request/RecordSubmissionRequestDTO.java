package com.bgdl.bgdl.models.dto.request;

import com.bgdl.bgdl.models.dto.common.RecordSubmissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionRequestDTO extends RecordSubmissionDTO {
    private UUID userId;
    private UUID demonId;
}
