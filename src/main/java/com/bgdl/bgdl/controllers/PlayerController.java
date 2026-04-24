package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.PlayerDetailsResponse;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.models.response.RegionResponse;
import com.bgdl.bgdl.services.PlayerService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<PageResponse<PlayerSummaryResponse>> getPlayers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(required = false) BulgarianRegion region
    ) {
        return ResponseEntity.ok(playerService.getPlayers(name, region, page));
    }

    @GetMapping("/regions")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<List<RegionResponse>> getRegions() {
        return ResponseEntity.ok(playerService.getAvailableRegions());
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "general_api_rate_limiter")
    public ResponseEntity<PlayerDetailsResponse> getPlayer(@PathVariable UUID id) {
        return ResponseEntity.ok(playerService.getPlayerDetails(id));
    }
}
