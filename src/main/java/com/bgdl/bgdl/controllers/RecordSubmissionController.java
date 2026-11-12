package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
import com.bgdl.bgdl.services.RecordSubmissionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordSubmissionController {
    private final RecordSubmissionService recordSubmissionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<List<RecordSubmissionResponse>> getAll() {
        return ResponseEntity.ok(recordSubmissionService.getAll());
    }

    @PostMapping
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<RecordSubmissionResponse> create(@RequestBody RecordSubmissionRequest recordSubmissionRequest) {
        return ResponseEntity.ok(recordSubmissionService.create(recordSubmissionRequest));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "moderation_rate_limiter")
    public ResponseEntity<RecordSubmissionResponse> createAccepted(@RequestBody RecordSubmissionRequest request) {
        return ResponseEntity.ok(recordSubmissionService.createAccepted(request));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "moderation_rate_limiter")
    public ResponseEntity<RecordSubmissionResponse> update(@RequestBody RecordSubmissionRequest recordSubmissionRequest) {
        return ResponseEntity.ok(recordSubmissionService.update(recordSubmissionRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "moderation_rate_limiter")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recordSubmissionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
