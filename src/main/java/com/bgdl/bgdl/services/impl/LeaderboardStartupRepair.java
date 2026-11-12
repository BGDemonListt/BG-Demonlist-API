package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class LeaderboardStartupRepair {
    private final PlayerRepository playerRepository;
    private final LeaderboardService leaderboardService;

    @EventListener(ApplicationReadyEvent.class)
    public void repairAfterRestart() {
        leaderboardService.requestRebuild(Set.of(), playerRepository.findActivePlayerIds());
    }
}
