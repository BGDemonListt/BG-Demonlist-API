package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.request.SkillsetTagRequest;
import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import com.bgdl.bgdl.services.SkillsetTagService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skillset-tags")
@RequiredArgsConstructor
public class SkillsetTagController {
    private final SkillsetTagService skillsetTagService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<List<SkillsetTagResponse>> getAll() {
        return ResponseEntity.ok(skillsetTagService.getAllTags());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<SkillsetTagResponse> create(@Valid @RequestBody SkillsetTagRequest request) {
        return ResponseEntity.ok(skillsetTagService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<SkillsetTagResponse> update(@PathVariable UUID id, @Valid @RequestBody SkillsetTagRequest request) {
        return ResponseEntity.ok(skillsetTagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        skillsetTagService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
