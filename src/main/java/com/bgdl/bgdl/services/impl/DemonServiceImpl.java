package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.demon.DemonCreateException;
import com.bgdl.bgdl.exceptions.demon.DemonNotFoundException;
import com.bgdl.bgdl.models.dto.request.DemonRequestDTO;
import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.services.DemonService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Component
@AllArgsConstructor
public class DemonServiceImpl implements DemonService {
    private final ModelMapper modelMapper;
    private final DemonRepository demonRepository;

    @Override
    public List<DemonResponseDTO> getAllDemons() {
        return demonRepository
                .findAllByDeletedAtIsNullOrderByPositionAsc()
                .stream()
                .map(
                        x -> modelMapper.map(x, DemonResponseDTO.class)
                )
                .toList();
    }

    @Override
    public Demon getById(UUID id) {
        return getEntityById(id, true);
    }

    @Override
    public DemonResponseDTO getDemonByLevelId(long levelId) {
        return modelMapper.map(getEntityByLevelId(levelId), DemonResponseDTO.class);
    }

    @Override
    public DemonResponseDTO createDemon(DemonRequestDTO demonRequestDTO) {
        if (demonRepository.findByDeletedAtIsNullAndLevelId(demonRequestDTO.getLevelId()).isPresent()) {
            throw new DemonCreateException(true);
        }

        Demon demon = modelMapper.map(demonRequestDTO, Demon.class);
        demon.setId(null);

        try {
            Demon savedDemon = demonRepository.save(demon);
            return modelMapper.map(savedDemon, DemonResponseDTO.class);
        } catch (Exception e) {
            throw new DemonCreateException(false);
        }
    }

    @Override
    public DemonResponseDTO update(long levelId, DemonRequestDTO demonRequestDTO) {
        Demon demon = getEntityByLevelId(levelId);
        Optional<Demon> potentialDemon = demonRepository.findByDeletedAtIsNullAndLevelIdAndIdNot(levelId, demon.getId());

        if (potentialDemon.isPresent()) {
            throw new DemonCreateException(true);
        }

        modelMapper.map(demonRequestDTO, demon);

        Demon updatedDemon = demonRepository.save(demon);
        return modelMapper.map(updatedDemon, DemonResponseDTO.class);
    }

    @Override
    public void delete(UUID id) {
        Demon demon = getEntityById(id);

        if (demon.getDeletedAt() == null) {
            demon.setDeletedAt(LocalDateTime.now());
        } else {
            demon.setDeletedAt(null);
        }

        demonRepository.save(demon);
    }

    private Demon getEntityById(UUID id) {
        Optional<Demon> demon = demonRepository.findById(id);

        if (demon.isEmpty()) {
            throw new DemonNotFoundException();
        }

        return demon.get();
    }

    private Demon getEntityById(UUID id, boolean deletedCheck) {
        Demon demon = getEntityById(id);
        if (deletedCheck && demon.getDeletedAt() != null) {
            throw new DemonNotFoundException();
        }

        return demon;
    }

    private Demon getEntityByLevelId(long levelId) {
        Optional<Demon> demon = demonRepository.findByDeletedAtIsNullAndLevelId(levelId);

        if (demon.isEmpty()) {
            throw new DemonNotFoundException();
        }

        return demon.get();
    }
}
