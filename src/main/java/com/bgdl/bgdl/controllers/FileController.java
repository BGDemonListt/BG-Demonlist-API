package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.services.FileService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private static final Duration CACHE_DURATION = Duration.ofDays(30);

    private final FileService fileService;

    @GetMapping("/{*filePath}")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<Resource> getFile(@PathVariable String filePath) {
        Resource resource = fileService.read(filePath);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_DURATION).cachePublic())
                .contentType(contentType)
                .body(resource);
    }
}
