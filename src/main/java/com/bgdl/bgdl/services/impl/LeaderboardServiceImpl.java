package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.models.response.PlayerResponse;
import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private PlayerService playerService;

    @Override
    public int updatePlayerPosition(double oldPoints, double newPoints) {
        Optional<PlayerResponse> closest = playerService.findClosestRelativePlayer(oldPoints, newPoints);
        int newPos;

        if (newPoints > oldPoints) {
            // Gains points → look above
            newPos = closest.map(PlayerResponse::getRank).orElse(1); // top if no one above

            playerService.shiftDownBetween(newPoints, oldPoints);
        } else {
            // Loses points → look below
            newPos = closest.map(PlayerResponse::getRank)
                    .orElse(playerService.getLastPosition()); // bottom if no one below

            playerService.shiftUpBetween(newPoints, oldPoints);
        }

        return newPos;
    }
}
