package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardRebuildWorkerTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RecordSubmissionRepository recordSubmissionRepository;

    @InjectMocks
    private LeaderboardRebuildWorker leaderboardRebuildWorker;

    @Test
    void rebuildLeaderboardRecomputesCompletedDemonsPointsHardestAndRanks() {
        Player firstPlayer = player("Alpha");
        Player secondPlayer = player("Beta");
        Player emptyPlayer = player("Gamma");

        Demon topDemon = demon("Top", 1, 323.0, false);
        Demon midDemon = demon("Mid", 2, 150.0, false);
        Demon removedDemon = demon("Removed", 3, 500.0, true);

        RecordSubmission alphaTop = submission(firstPlayer, topDemon);
        RecordSubmission alphaDuplicate = submission(firstPlayer, topDemon);
        RecordSubmission betaMid = submission(secondPlayer, midDemon);
        RecordSubmission removed = submission(firstPlayer, removedDemon);

        when(playerRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(firstPlayer, secondPlayer, emptyPlayer));
        when(recordSubmissionRepository.findAllByDeletedAtIsNullAndStatus(RecordSubmissionStatus.ACCEPTED))
                .thenReturn(List.of(alphaTop, alphaDuplicate, betaMid, removed));

        leaderboardRebuildWorker.rebuildLeaderboard();

        assertEquals(323.0, firstPlayer.getPoints());
        assertEquals(1, firstPlayer.getRank());
        assertEquals("Top", firstPlayer.getHardestDemon().getLevelTitle());
        assertEquals(1, firstPlayer.getCompletedDemons().size());

        assertEquals(150.0, secondPlayer.getPoints());
        assertEquals(2, secondPlayer.getRank());
        assertEquals("Mid", secondPlayer.getHardestDemon().getLevelTitle());

        assertEquals(0.0, emptyPlayer.getPoints());
        assertNull(emptyPlayer.getRank());
        assertNull(emptyPlayer.getHardestDemon());
        assertEquals(0, emptyPlayer.getCompletedDemons().size());

        verify(playerRepository).saveAll(anyList());
    }

    private Player player(String name) {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setName(name);
        player.setPoints(0.0);
        player.setCompletedDemons(new LinkedHashSet<>());
        return player;
    }

    private Demon demon(String title, int position, double points, boolean deleted) {
        Demon demon = new Demon();
        demon.setId(UUID.randomUUID());
        demon.setLevelTitle(title);
        demon.setLevelId(position * 1000L);
        demon.setPosition(position);
        demon.setPoints(points);
        if (deleted) {
            demon.setDeletedAt(LocalDateTime.now());
        }
        return demon;
    }

    private RecordSubmission submission(Player holder, Demon demon) {
        RecordSubmission submission = new RecordSubmission();
        submission.setId(UUID.randomUUID());
        submission.setHolder(holder);
        submission.setDemon(demon);
        submission.setStatus(RecordSubmissionStatus.ACCEPTED);
        return submission;
    }
}
