package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.dto.request.DemonRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.services.DemonService;
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
@RequestMapping("/api/v1/demons")
@RequiredArgsConstructor
public class DemonController {
    private final DemonService demonService;

    @GetMapping
    public ResponseEntity<List<DemonResponseDTO>> getAll() {
        List<DemonResponseDTO> demons = demonService.getAllDemons();
        return ResponseEntity.ok(demons);
    }

    @GetMapping("/{levelId}")
    public ResponseEntity<DemonResponseDTO> getByLevelId(@PathVariable long levelId) {
        DemonResponseDTO demon = demonService.getDemonByLevelId(levelId);
        return ResponseEntity.ok(demon);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<DemonResponseDTO> create(@RequestBody DemonRequestDTO demonRequestDTO) {
        DemonResponseDTO demon = demonService.createDemon(demonRequestDTO);
        return ResponseEntity.ok(demon);
    }

    @PutMapping("/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<DemonResponseDTO> update(
            @PathVariable long levelId,
            @Valid @RequestBody DemonRequestDTO demonRequestDTO) {
        DemonResponseDTO demon = demonService.update(levelId, demonRequestDTO);
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
