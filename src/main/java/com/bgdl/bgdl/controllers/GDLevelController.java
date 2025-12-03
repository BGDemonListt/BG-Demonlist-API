package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.response.gdApi.GDLevelResponse;
import com.bgdl.bgdl.services.GdService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gd/api")
@RequiredArgsConstructor
public class GDLevelController {
    private final GdService gdService;

    @GetMapping("/{levelId}")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<GDLevelResponse> getGdLevelById(@PathVariable String levelId) {
        GDLevelResponse gdLevel = gdService.getGDLevelById(levelId);
        return ResponseEntity.ok(gdLevel);
    }
}
