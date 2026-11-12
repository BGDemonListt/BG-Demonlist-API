package com.bgdl.bgdl.services;

import java.util.Set;
import java.util.UUID;

public interface LeaderboardService {
    void requestRebuild(Set<UUID> affectedDemonIds, Set<UUID> affectedPlayerIds);
}
