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
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordSubmissionServiceImpl extends BaseService<RecordSubmission, UUID> implements RecordSubmissionService {
    private final RecordSubmissionRepository recordSubmissionRepository;
    private final DemonService demonService;
    private final PlayerService playerService;
    private final LeaderboardService leaderboardService;
    private final EntityManager entityManager;

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
        return createWithStatus(request, RecordSubmissionStatus.PENDING);
    }

    @Override
    @Transactional
    public RecordSubmissionResponse createAccepted(RecordSubmissionRequest request) {
        return createWithStatus(request, RecordSubmissionStatus.ACCEPTED);
    }

    private RecordSubmissionResponse createWithStatus(RecordSubmissionRequest request, RecordSubmissionStatus status) {
        Player player = playerService.getById(request.getPlayerId());
        entityManager.lock(player, LockModeType.PESSIMISTIC_WRITE);
        boolean isDuplicate = recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(
                request.getPlayerId(),
                request.getDemonId()
        ).isPresent();

        if (isDuplicate) {
            throw new RecordSubmissionCreateException(true);
        }

        Demon demon = demonService.getById(request.getDemonId());

        RecordSubmission recordSubmission = new RecordSubmission();
        recordSubmission.setProgress(request.getProgress());
        recordSubmission.setYoutubeUrl(request.getYoutubeUrl());
        recordSubmission.setRawFootageUrl(request.getRawFootageUrl());
        recordSubmission.setDescription(request.getDescription());
        recordSubmission.setStatus(status);
        recordSubmission.setDemon(demon);
        recordSubmission.setHolder(player);

        RecordSubmission savedRecordSubmission = recordSubmissionRepository.save(recordSubmission);
        if (status == RecordSubmissionStatus.ACCEPTED) {
            leaderboardService.requestRebuild(Set.of(), Set.of(player.getId()));
        }
        return toResponse(savedRecordSubmission);
    }

    @Override
    @Transactional
    public RecordSubmissionResponse update(RecordSubmissionRequest request) {
        RecordSubmission submission = recordSubmissionRepository.findByIdForUpdate(request.getId())
                .filter(record -> record.getDeletedAt() == null)
                .orElseThrow(this::notFoundException);
        RecordSubmissionStatus previousStatus = submission.getStatus();
        RecordSubmissionStatus nextStatus = request.getStatus();

        if (nextStatus != null) {
            submission.setStatus(nextStatus);
        }

        RecordSubmission savedSubmission = recordSubmissionRepository.save(submission);

        if (previousStatus == RecordSubmissionStatus.ACCEPTED ^ savedSubmission.getStatus() == RecordSubmissionStatus.ACCEPTED) {
            leaderboardService.requestRebuild(Set.of(), Set.of(savedSubmission.getHolder().getId()));
        }

        return toResponse(savedSubmission);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        RecordSubmission submission = recordSubmissionRepository.findByIdForUpdate(id)
                .orElseThrow(this::notFoundException);
        if (submission.getDeletedAt() != null) {
            entityManager.lock(submission.getHolder(), LockModeType.PESSIMISTIC_WRITE);
            if (recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(
                    submission.getHolder().getId(), submission.getDemon().getId()).isPresent()) {
                throw new RecordSubmissionCreateException(true);
            }
        }
        boolean affectsLeaderboard = submission.getStatus() == RecordSubmissionStatus.ACCEPTED;

        super.delete(id);

        if (affectsLeaderboard) {
            leaderboardService.requestRebuild(Set.of(), Set.of(submission.getHolder().getId()));
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
