package com.bgdl.bgdl.exceptions.recordSubmissions;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class RecordSubmissionCreateException extends BadRequestException {
    public RecordSubmissionCreateException(boolean isUnique) {
        super(
                isUnique
                        ? "Вече съществува заявка със същото ID!"
                        : "Невалидни данни за заявката!"
        );
    }
}
