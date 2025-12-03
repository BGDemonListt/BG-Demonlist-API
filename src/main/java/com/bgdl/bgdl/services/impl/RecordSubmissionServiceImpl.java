package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.RecordSubmissionResponse;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.RecordSubmissionService;
import com.bgdl.bgdl.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordSubmissionServiceImpl implements RecordSubmissionService {
    private final RecordSubmissionRepository recordSubmissionRepository;
    private final ModelMapper modelMapper;
    private final DemonService demonService;
    private final UserService userService;

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
                recordSubmissionRequest.getUserId(),
                recordSubmissionRequest.getDemonId())
                .isPresent();

        if (isDuplicate) {
            throw new IllegalArgumentException();
        }

        User user = userService.findById(recordSubmissionRequest.getUserId());
        Demon demon = demonService.getById(recordSubmissionRequest.getDemonId());

        RecordSubmission recordSubmission = modelMapper.map(recordSubmissionRequest, RecordSubmission.class);
        recordSubmission.setId(null);
//        recordSubmission.setDemon(demon);
//        recordSubmission.setHolder(user);

        try {
            RecordSubmission savedRecordSubmission = recordSubmissionRepository.save(recordSubmission);
            return modelMapper.map(savedRecordSubmission, RecordSubmissionResponse.class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public RecordSubmissionResponse update(RecordSubmissionRequest recordSubmissionRequest) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    private RecordSubmission getEntityById(UUID id) {
        Optional<RecordSubmission> record = recordSubmissionRepository.findById(id);

        if (record.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return record.get();
    }

    private RecordSubmission getEntityById(UUID id, boolean deletedCheck) {
        RecordSubmission record = getEntityById(id);

        if (deletedCheck && record.getDeletedAt() != null) {
            throw new IllegalArgumentException();
        }

        return record;
    }
}
