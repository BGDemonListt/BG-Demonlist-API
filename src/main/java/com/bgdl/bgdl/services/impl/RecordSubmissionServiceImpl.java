package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.demon.DemonNotFoundException;
import com.bgdl.bgdl.models.dto.request.RecordSubmissionRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.models.dto.response.RecordSubmissionResponseDTO;
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
    public List<RecordSubmissionResponseDTO> getAll() {
        return recordSubmissionRepository
                .findAllByDeletedAtIsNull()
                .stream()
                .map(
                        x -> modelMapper.map(x, RecordSubmissionResponseDTO.class)
                )
                .toList();
    }

    @Override
    public RecordSubmissionResponseDTO create(RecordSubmissionRequestDTO recordSubmissionRequestDTO) {
        boolean isDuplicate = recordSubmissionRepository.findByDeletedAtIsNullAndHolderIdAndDemonId(
                recordSubmissionRequestDTO.getUserId(),
                recordSubmissionRequestDTO.getDemonId())
                .isPresent();

        if (isDuplicate) {
            throw new IllegalArgumentException();
        }

        User user = userService.findById(recordSubmissionRequestDTO.getUserId());
        Demon demon = demonService.getById(recordSubmissionRequestDTO.getDemonId());

        RecordSubmission recordSubmission = modelMapper.map(recordSubmissionRequestDTO, RecordSubmission.class);
        recordSubmission.setId(null);
//        recordSubmission.setDemon(demon);
//        recordSubmission.setHolder(user);

        try {
            RecordSubmission savedRecordSubmission = recordSubmissionRepository.save(recordSubmission);
            return modelMapper.map(savedRecordSubmission, RecordSubmissionResponseDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public RecordSubmissionResponseDTO update(RecordSubmissionRequestDTO recordSubmissionRequestDTO) {
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
