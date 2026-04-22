package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.exceptions.common.NoSuchElementException;
import com.bgdl.bgdl.exceptions.demon.DemonNotFoundException;
import com.bgdl.bgdl.exceptions.recordSubmissions.RecordSubmissionCreateException;
import com.bgdl.bgdl.exceptions.recordSubmissions.RecordSubmissionsNotFoundException;
import com.bgdl.bgdl.handlers.events.leaderboard.OnSubmissionAcceptEvent;
import com.bgdl.bgdl.handlers.events.leaderboard.OnSubmissionRejectEvent;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordSubmissionServiceImpl extends BaseService<RecordSubmission, UUID> implements RecordSubmissionService {
    private final RecordSubmissionRepository recordSubmissionRepository;
    private final ModelMapper modelMapper;
    private final DemonService demonService;
    private final PlayerService playerService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<RecordSubmissionResponse> getAll() {
        return recordSubmissionRepository
                .findAllByDeletedAtIsNull()
                .stream()
                .map(
                        x -> modelMapper.map(x, RecordSubmissionResponse.class)
                )
                .toList();
    }

    @Override
    public RecordSubmissionResponse create(RecordSubmissionRequest recordSubmissionRequest) {
        boolean isDuplicate = recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(
                recordSubmissionRequest.getPlayerId(),
                recordSubmissionRequest.getDemonId())
                .isPresent();

        if (isDuplicate) {
            throw new RecordSubmissionCreateException(true);
        }

        Player player = playerService.getById(recordSubmissionRequest.getPlayerId());
        Demon demon = demonService.getById(recordSubmissionRequest.getDemonId());

        RecordSubmission recordSubmission = modelMapper.map(recordSubmissionRequest, RecordSubmission.class);
        recordSubmission.setId(null);
        recordSubmission.setDemon(demon);
        recordSubmission.setHolder(player);

        try {
            RecordSubmission savedRecordSubmission = recordSubmissionRepository.save(recordSubmission);
            return modelMapper.map(savedRecordSubmission, RecordSubmissionResponse.class);
        } catch (Exception e) {
            throw new RecordSubmissionCreateException(false);
        }
    }

    @Override
    public RecordSubmissionResponse update(RecordSubmissionRequest request) {
        RecordSubmission submission = getEntityById(request.getId(), true);
        RecordSubmissionStatus oldStatus = submission.getStatus();
        RecordSubmissionStatus newStatus = request.getStatus();

        boolean wasAccepted = oldStatus == RecordSubmissionStatus.ACCEPTED;
        boolean becomesAccepted = newStatus == RecordSubmissionStatus.ACCEPTED;

        boolean wasPendingOrRejected = oldStatus == RecordSubmissionStatus.PENDING
                || oldStatus == RecordSubmissionStatus.REJECTED;

        boolean becomesPendingOrRejected = newStatus == RecordSubmissionStatus.PENDING
                || newStatus == RecordSubmissionStatus.REJECTED;

        if (wasPendingOrRejected && becomesAccepted) {
            // 1. pending/rejected → accepted
            eventPublisher.publishEvent(
                    new OnSubmissionAcceptEvent(this, submission.getHolder(), submission.getDemon())
            );
            submission.setStatus(newStatus);
        } else if (wasAccepted && becomesPendingOrRejected) {
            // 2. accepted → pending/rejected
            eventPublisher.publishEvent(
                    new OnSubmissionRejectEvent(this, submission.getHolder(), submission.getDemon())
            );
            submission.setStatus(newStatus);
        } else if (wasPendingOrRejected && becomesPendingOrRejected && oldStatus != newStatus) {
            // 3. pending ↔ rejected (only update status, no event)
            submission.setStatus(newStatus);
        }

        // 4. All other transitions → no-op (status stays same)
        return modelMapper.map(recordSubmissionRepository.save(submission), RecordSubmissionResponse.class);
    }

    @Override
    public void delete(UUID id) {
        super.delete(id);
    }

    @Override
    protected JpaRepository<RecordSubmission, UUID> getRepository() {
        return recordSubmissionRepository;
    }

    @Override
    protected NoSuchElementException notFoundException() {
        return new RecordSubmissionsNotFoundException();
    }
}
