package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import com.bgdl.bgdl.models.dto.RecordSubmissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionResponse extends RecordSubmissionDTO {
    private UUID id;
    private PlayerSummaryResponse holder;
    private DemonBaseDTO demon;
}
