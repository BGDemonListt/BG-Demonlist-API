package com.bgdl.bgdl.models.request;

import com.bgdl.bgdl.models.dto.RecordSubmissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionRequest extends RecordSubmissionDTO {
    private UUID userId;
    private UUID demonId;
}
