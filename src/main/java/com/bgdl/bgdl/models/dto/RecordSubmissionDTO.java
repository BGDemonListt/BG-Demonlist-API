package com.bgdl.bgdl.models.dto;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class RecordSubmissionDTO {
    private int progress;
    private String youtubeUrl;
    private String rawFootageUrl;
    private String description;
    private RecordSubmissionStatus status;
}
