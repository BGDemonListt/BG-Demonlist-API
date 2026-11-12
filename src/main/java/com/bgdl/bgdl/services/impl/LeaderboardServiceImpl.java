package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private final LeaderboardRebuildQueue leaderboardRebuildQueue;

    @Override
    public void requestRebuild(Set<UUID> affectedDemonIds, Set<UUID> affectedPlayerIds) {
        leaderboardRebuildQueue.requestRebuild(affectedDemonIds, affectedPlayerIds);
    }
}
