package com.bgdl.bgdl.models.dto.common;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RecordSubmissionDTO extends BaseDTO {
    private int progress;
    private String youtubeUrl;
    private String rawFootageUrl;
    private String description;
    private RecordSubmissionStatus status;
}
