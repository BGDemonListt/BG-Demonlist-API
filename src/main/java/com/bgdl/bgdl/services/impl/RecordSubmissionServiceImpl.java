package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.exceptions.common.NoSuchElementException;
import com.bgdl.bgdl.exceptions.recordSubmissions.RecordSubmissionCreateException;
import com.bgdl.bgdl.exceptions.recordSubmissions.RecordSubmissionsNotFoundException;
import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.RecordSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordSubmissionServiceImpl extends BaseService<RecordSubmission, UUID> implements RecordSubmissionService {
    private final RecordSubmissionRepository recordSubmissionRepository;
    private final DemonService demonService;
    private final PlayerService playerService;
    private final LeaderboardService leaderboardService;

    @Override
    @Transactional(readOnly = true)
    public List<RecordSubmissionResponse> getAll() {
        return recordSubmissionRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecordSubmissionResponse create(RecordSubmissionRequest request) {
        boolean isDuplicate = recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(
                request.getPlayerId(),
                request.getDemonId()
        ).isPresent();

        if (isDuplicate) {
            throw new RecordSubmissionCreateException(true);
        }

        Player player = playerService.getById(request.getPlayerId());
        Demon demon = demonService.getById(request.getDemonId());

        RecordSubmission recordSubmission = new RecordSubmission();
        recordSubmission.setProgress(request.getProgress());
        recordSubmission.setYoutubeUrl(request.getYoutubeUrl());
        recordSubmission.setRawFootageUrl(request.getRawFootageUrl());
        recordSubmission.setDescription(request.getDescription());
        recordSubmission.setStatus(RecordSubmissionStatus.PENDING);
        recordSubmission.setDemon(demon);
        recordSubmission.setHolder(player);

        try {
            RecordSubmission savedRecordSubmission = recordSubmissionRepository.save(recordSubmission);
            return toResponse(savedRecordSubmission);
        } catch (RuntimeException exception) {
            throw new RecordSubmissionCreateException(false);
        }
    }

    @Override
    @Transactional
    public RecordSubmissionResponse update(RecordSubmissionRequest request) {
        RecordSubmission submission = getEntityById(request.getId(), true);
        RecordSubmissionStatus previousStatus = submission.getStatus();
        RecordSubmissionStatus nextStatus = request.getStatus();

        if (nextStatus != null) {
            submission.setStatus(nextStatus);
        }

        RecordSubmission savedSubmission = recordSubmissionRepository.save(submission);

        if (previousStatus == RecordSubmissionStatus.ACCEPTED ^ savedSubmission.getStatus() == RecordSubmissionStatus.ACCEPTED) {
            leaderboardService.rebuildLeaderboard();
        }

        return toResponse(getEntityById(savedSubmission.getId(), true));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        RecordSubmission submission = getEntityById(id, false);
        boolean affectsLeaderboard = submission.getDeletedAt() == null
                ? submission.getStatus() == RecordSubmissionStatus.ACCEPTED
                : submission.getDeletedAt() != null && submission.getStatus() == RecordSubmissionStatus.ACCEPTED;

        super.delete(id);

        if (affectsLeaderboard) {
            leaderboardService.rebuildLeaderboard();
        }
    }

    @Override
    protected JpaRepository<RecordSubmission, UUID> getRepository() {
        return recordSubmissionRepository;
    }

    @Override
    protected NoSuchElementException notFoundException() {
        return new RecordSubmissionsNotFoundException();
    }

    private RecordSubmissionResponse toResponse(RecordSubmission submission) {
        RecordSubmissionResponse response = new RecordSubmissionResponse();
        response.setId(submission.getId());
        response.setProgress(submission.getProgress());
        response.setYoutubeUrl(submission.getYoutubeUrl());
        response.setRawFootageUrl(submission.getRawFootageUrl());
        response.setDescription(submission.getDescription());
        response.setStatus(submission.getStatus());
        response.setHolder(toPlayerSummary(submission.getHolder()));
        response.setDemon(toDemonBase(submission.getDemon()));
        return response;
    }

    private PlayerSummaryResponse toPlayerSummary(Player player) {
        PlayerSummaryResponse response = new PlayerSummaryResponse();
        response.setId(player.getId());
        response.setName(player.getName());
        response.setPoints(player.getPoints());
        response.setPosition(player.getRank());
        return response;
    }

    private DemonBaseDTO toDemonBase(Demon demon) {
        DemonBaseDTO response = new DemonBaseDTO();
        response.setId(demon.getId());
        response.setLevelTitle(demon.getLevelTitle());
        response.setLevelId(demon.getLevelId());
        return response;
    }
}
