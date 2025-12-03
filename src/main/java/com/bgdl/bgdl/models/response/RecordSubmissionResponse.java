package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.models.dto.DemonDTO;
import com.bgdl.bgdl.models.dto.RecordSubmissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionResponse extends RecordSubmissionDTO {
    private PublicUserResponse userId;
    private DemonDTO demonId;
}
