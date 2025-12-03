package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.dto.request.RecordSubmissionRequestDTO;
import com.bgdl.bgdl.models.dto.response.RecordSubmissionResponseDTO;
import com.bgdl.bgdl.services.RecordSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordSubmissionController {
    private final RecordSubmissionService recordSubmissionService;

    @GetMapping
    public ResponseEntity<List<RecordSubmissionResponseDTO>> getAll() {
        return ResponseEntity.ok(recordSubmissionService.getAll());
    }

    @PostMapping
    public ResponseEntity<RecordSubmissionResponseDTO> create(@RequestBody RecordSubmissionRequestDTO recordSubmissionRequestDTO) {
        return ResponseEntity.ok(recordSubmissionService.create(recordSubmissionRequestDTO));
    }
}
