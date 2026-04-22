package com.bgdl.bgdl.exceptions.recordSubmissions;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class RecordSubmissionCreateException extends BadRequestException {
    public RecordSubmissionCreateException(boolean isUnique) {
        super(
                isUnique
                        ? "Submission with the same id already exists!"
                        : "Invalid record submission data!"
        );
    }
}
