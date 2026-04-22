package com.bgdl.bgdl.handlers.events.leaderboard;

import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LeaderboardEventsListener {
    private final LeaderboardService leaderboardService;
    private final PlayerService playerService;

    @EventListener
    @Transactional
    public void handleSubmissionAcceptEvent(OnSubmissionAcceptEvent event) {
        var holder = event.getHolder();
        var demon = event.getBeatenDemon();

        int newPosition = leaderboardService.updatePlayerPosition(
                holder.getPoints(),
                holder.getPoints() + demon.getPoints()
        );

        playerService.beatDemon(holder, demon, newPosition);
    }

    @EventListener
    @Transactional
    public void handleSubmissionRejectEvent(OnSubmissionRejectEvent event) {
        var holder = event.getHolder();
        var demon = event.getRejectedDemon();

        int newPosition = leaderboardService.updatePlayerPosition(
                holder.getPoints(),
                holder.getPoints() - demon.getPoints()
        );

        playerService.removeDemon(holder, demon, newPosition);
    }
}
