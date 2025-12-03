package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
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
    public ResponseEntity<List<RecordSubmissionResponse>> getAll() {
        return ResponseEntity.ok(recordSubmissionService.getAll());
    }

    @PostMapping
    public ResponseEntity<RecordSubmissionResponse> create(@RequestBody RecordSubmissionRequest recordSubmissionRequest) {
        return ResponseEntity.ok(recordSubmissionService.create(recordSubmissionRequest));
    }
}
