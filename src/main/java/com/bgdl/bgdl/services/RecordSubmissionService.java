package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface RecordSubmissionService {
    List<RecordSubmissionResponse> getAll();

    RecordSubmissionResponse create(RecordSubmissionRequest recordSubmissionRequest);

    RecordSubmissionResponse update(RecordSubmissionRequest recordSubmissionRequest);

    void delete(UUID id);
}
