package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private final LeaderboardRebuildQueue leaderboardRebuildQueue;

    @Override
    public void requestRebuild() {
        leaderboardRebuildQueue.requestRebuild();
    }
}
