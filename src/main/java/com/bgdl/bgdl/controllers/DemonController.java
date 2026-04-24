package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.models.response.DemonSummaryResponse;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.services.DemonService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demons")
@RequiredArgsConstructor
public class DemonController {
    private final DemonService demonService;

    @GetMapping
    public ResponseEntity<PageResponse<DemonSummaryResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String name
    ) {
        PageResponse<DemonSummaryResponse> demons = demonService.getAllDemons(name, page);
        return ResponseEntity.ok(demons);
    }

    @GetMapping("/{levelId}")
    public ResponseEntity<DemonResponse> getByLevelId(@PathVariable long levelId) {
        DemonResponse demon = demonService.getDemonByLevelId(levelId);
        return ResponseEntity.ok(demon);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<DemonResponse> create(@Valid @RequestBody DemonRequest demonRequest) {
        DemonResponse demon = demonService.createDemon(demonRequest);
        return ResponseEntity.ok(demon);
    }

    @PutMapping("/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<DemonResponse> update(
            @PathVariable long levelId,
            @Valid @RequestBody DemonRequest demonRequest) {
        DemonResponse demon = demonService.update(levelId, demonRequest);
        return ResponseEntity.ok(demon);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        demonService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
