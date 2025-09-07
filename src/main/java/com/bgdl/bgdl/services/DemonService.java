package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.dto.request.DemonRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;

import java.util.List;
import java.util.UUID;

public interface DemonService {
    List<DemonResponseDTO> getAllDemons();

    DemonResponseDTO getDemonByLevelId(long levelId);

    DemonResponseDTO createDemon(DemonRequestDTO demonRequestDTO);

    DemonResponseDTO update(long levelId, DemonRequestDTO demonRequestDTO);

    void delete(UUID id);
}
