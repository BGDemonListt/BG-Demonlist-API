package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordSubmissionServiceImplTest {

    @Mock
    private RecordSubmissionRepository recordSubmissionRepository;

    @Mock
    private DemonService demonService;

    @Mock
    private PlayerService playerService;

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private RecordSubmissionServiceImpl recordSubmissionService;

    @Test
    void createAlwaysPersistsPendingSubmission() {
        UUID playerId = UUID.randomUUID();
        UUID demonId = UUID.randomUUID();
        Player player = player(playerId, "Player One");
        Demon demon = demon(demonId, "Top Demon");

        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setPlayerId(playerId);
        request.setDemonId(demonId);
        request.setProgress(100);
        request.setYoutubeUrl("https://youtube.com/watch?v=demo");
        request.setRawFootageUrl("https://drive.google.com/demo");
        request.setDescription("Completion");
        request.setStatus(RecordSubmissionStatus.ACCEPTED);

        when(recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(playerId, demonId))
                .thenReturn(Optional.empty());
        when(playerService.getById(playerId)).thenReturn(player);
        when(demonService.getById(demonId)).thenReturn(demon);
        when(recordSubmissionRepository.save(any(RecordSubmission.class))).thenAnswer(invocation -> {
            RecordSubmission saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        RecordSubmissionResponse response = recordSubmissionService.create(request);

        assertEquals(RecordSubmissionStatus.PENDING, response.getStatus());
        assertEquals("Player One", response.getHolder().getName());
        assertEquals("Top Demon", response.getDemon().getLevelTitle());
        assertNotNull(response.getId());
        verify(leaderboardService, never()).rebuildLeaderboard();
    }

    @Test
    void updateRebuildsLeaderboardWhenSubmissionBecomesAccepted() {
        UUID submissionId = UUID.randomUUID();
        RecordSubmission submission = submission(submissionId, RecordSubmissionStatus.PENDING);
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setId(submissionId);
        request.setStatus(RecordSubmissionStatus.ACCEPTED);

        when(recordSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(recordSubmissionRepository.save(any(RecordSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordSubmissionResponse response = recordSubmissionService.update(request);

        assertEquals(RecordSubmissionStatus.ACCEPTED, response.getStatus());
        verify(leaderboardService).rebuildLeaderboard();
    }

    @Test
    void updateRebuildsLeaderboardWhenSubmissionLosesAcceptedStatus() {
        UUID submissionId = UUID.randomUUID();
        RecordSubmission submission = submission(submissionId, RecordSubmissionStatus.ACCEPTED);
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setId(submissionId);
        request.setStatus(RecordSubmissionStatus.REJECTED);

        when(recordSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(recordSubmissionRepository.save(any(RecordSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordSubmissionResponse response = recordSubmissionService.update(request);

        assertEquals(RecordSubmissionStatus.REJECTED, response.getStatus());
        verify(leaderboardService).rebuildLeaderboard();
    }

    @Test
    void updateDoesNotRebuildLeaderboardForPendingToRejectedTransition() {
        UUID submissionId = UUID.randomUUID();
        RecordSubmission submission = submission(submissionId, RecordSubmissionStatus.PENDING);
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setId(submissionId);
        request.setStatus(RecordSubmissionStatus.REJECTED);

        when(recordSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(recordSubmissionRepository.save(any(RecordSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordSubmissionResponse response = recordSubmissionService.update(request);

        assertEquals(RecordSubmissionStatus.REJECTED, response.getStatus());
        verify(leaderboardService, never()).rebuildLeaderboard();
    }

    @Test
    void deleteRebuildsLeaderboardWhenAcceptedSubmissionIsRemoved() {
        UUID submissionId = UUID.randomUUID();
        RecordSubmission submission = submission(submissionId, RecordSubmissionStatus.ACCEPTED);

        when(recordSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(recordSubmissionRepository.save(any(RecordSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recordSubmissionService.delete(submissionId);

        verify(recordSubmissionRepository).save(submission);
        verify(leaderboardService).rebuildLeaderboard();
    }

    private RecordSubmission submission(UUID id, RecordSubmissionStatus status) {
        RecordSubmission submission = new RecordSubmission();
        submission.setId(id);
        submission.setStatus(status);
        submission.setHolder(player(UUID.randomUUID(), "Player One"));
        submission.setDemon(demon(UUID.randomUUID(), "Top Demon"));
        submission.setProgress(100);
        submission.setYoutubeUrl("https://youtube.com/watch?v=demo");
        submission.setRawFootageUrl("https://drive.google.com/demo");
        submission.setDescription("Completion");
        return submission;
    }

    private Player player(UUID id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setPoints(100.0);
        player.setRank(1);
        return player;
    }

    private Demon demon(UUID id, String title) {
        Demon demon = new Demon();
        demon.setId(id);
        demon.setLevelTitle(title);
        demon.setLevelId(123L);
        demon.setPoints(323.0);
        demon.setPosition(1);
        return demon;
    }
}
