package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.dto.common.RecordSubmissionDTO;
import com.bgdl.bgdl.models.dto.request.DemonRequestDTO;
import com.bgdl.bgdl.models.dto.request.RecordSubmissionRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.models.dto.response.RecordSubmissionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface RecordSubmissionService {
    List<RecordSubmissionResponseDTO> getAll();

    RecordSubmissionResponseDTO create(RecordSubmissionRequestDTO recordSubmissionRequestDTO);

    RecordSubmissionResponseDTO update(RecordSubmissionRequestDTO recordSubmissionRequestDTO);

    void delete(UUID id);
}
