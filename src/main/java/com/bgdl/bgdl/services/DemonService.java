package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.dto.request.DemonRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.models.entity.Demon;

import java.util.List;
import java.util.UUID;

public interface DemonService {
    List<DemonResponseDTO> getAllDemons();

    Demon getById(UUID id);

    DemonResponseDTO getDemonByLevelId(long levelId);

    DemonResponseDTO createDemon(DemonRequestDTO demonRequestDTO);

    DemonResponseDTO update(long levelId, DemonRequestDTO demonRequestDTO);

    void delete(UUID id);
}
