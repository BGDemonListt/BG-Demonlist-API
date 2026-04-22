package com.bgdl.bgdl.exceptions.recordSubmissions;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class RecordSubmissionsNotFoundException extends NoSuchElementException {
    public RecordSubmissionsNotFoundException() {
        super("The submission is not found!");
    }
}