package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;
import com.bgdl.bgdl.exceptions.demon.DemonCreateException;
import com.bgdl.bgdl.exceptions.demon.DemonInvalidPositionException;
import com.bgdl.bgdl.exceptions.demon.DemonNotFoundException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemonServiceImpl extends BaseService<Demon, UUID> implements DemonService {
    private final DemonRepository demonRepository;
    private final LeaderboardService leaderboardService;

    @Override
    @Transactional(readOnly = true)
    public List<DemonResponse> getAllDemons() {
        return demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Demon getById(UUID id) {
        return getEntityById(id, true);
    }

    @Override
    @Transactional(readOnly = true)
    public DemonResponse getDemonByLevelId(long levelId) {
        return toResponse(getEntityByLevelId(levelId));
    }

    @Override
    @Transactional
    public DemonResponse createDemon(DemonRequest demonRequest) {
        if (demonRepository.findByDeletedAtIsNullAndLevelId(demonRequest.getLevelId()).isPresent()) {
            throw new DemonCreateException(true);
        }

        Demon demon = mapRequestToDemon(new Demon(), demonRequest);
        List<Demon> reorderedDemons = reorderDemons(
                demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc(),
                demon,
                demonRequest.getPosition()
        );

        try {
            demonRepository.saveAll(reorderedDemons);
            leaderboardService.requestRebuild();
            return toResponse(demon);
        } catch (RuntimeException exception) {
            throw new DemonCreateException(false);
        }
    }

    @Override
    @Transactional
    public DemonResponse update(long levelId, DemonRequest demonRequest) {
        Demon demon = getEntityByLevelId(levelId);
        Optional<Demon> potentialDemon = demonRepository.findByDeletedAtIsNullAndLevelIdAndIdNot(
                demonRequest.getLevelId(),
                demon.getId()
        );

        if (potentialDemon.isPresent()) {
            throw new DemonCreateException(true);
        }

        mapRequestToDemon(demon, demonRequest);
        List<Demon> activeDemons = new ArrayList<>(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc());
        activeDemons.removeIf(existingDemon -> existingDemon.getId().equals(demon.getId()));

        List<Demon> reorderedDemons = reorderDemons(activeDemons, demon, demonRequest.getPosition());
        demonRepository.saveAll(reorderedDemons);
        leaderboardService.requestRebuild();
        return toResponse(demon);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Demon demon = getEntityById(id, false);
        super.delete(id);

        if (demon.getDeletedAt() != null) {
            rebalanceRemainingDemons(id);
        } else {
            List<Demon> activeDemons = new ArrayList<>(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc());
            activeDemons.removeIf(existingDemon -> existingDemon.getId().equals(demon.getId()));
            List<Demon> reorderedDemons = reorderDemons(activeDemons, demon, demon.getPosition());
            demonRepository.saveAll(reorderedDemons);
        }

        leaderboardService.requestRebuild();
    }

    @Override
    protected JpaRepository<Demon, UUID> getRepository() {
        return demonRepository;
    }

    @Override
    protected NoSuchElementException notFoundException() {
        return new DemonNotFoundException();
    }

    private Demon getEntityByLevelId(long levelId) {
        return demonRepository.findByDeletedAtIsNullAndLevelId(levelId)
                .orElseThrow(this::notFoundException);
    }

    private List<Demon> reorderDemons(List<Demon> demons, Demon targetDemon, int requestedPosition) {
        int boundedPosition = normalizePosition(requestedPosition, demons.size() + 1);
        demons.add(boundedPosition - 1, targetDemon);

        for (int index = 0; index < demons.size(); index++) {
            Demon demon = demons.get(index);
            demon.setPosition(index + 1);
            demon.recalculatePoints(demons.size());
        }

        return demons;
    }

    private void rebalanceRemainingDemons(UUID removedDemonId) {
        List<Demon> activeDemons = new ArrayList<>(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc());
        activeDemons.removeIf(existingDemon -> existingDemon.getId().equals(removedDemonId));

        for (int index = 0; index < activeDemons.size(); index++) {
            Demon demon = activeDemons.get(index);
            demon.setPosition(index + 1);
            demon.recalculatePoints(activeDemons.size());
        }

        demonRepository.saveAll(activeDemons);
    }

    private int normalizePosition(int requestedPosition, int maxAllowedPosition) {
        if (requestedPosition < 1 || requestedPosition > maxAllowedPosition) {
            throw new DemonInvalidPositionException();
        }

        return requestedPosition;
    }

    private Demon mapRequestToDemon(Demon demon, DemonRequest request) {
        demon.setLevelTitle(request.getLevelTitle());
        demon.setLevelId(request.getLevelId());
        demon.setCreatorName(request.getCreatorName());
        demon.setCreatorId(request.getCreatorId());
        demon.setDescription(request.getDescription());
        demon.setLevelPassword(request.getLevelPassword());
        demon.setMusicName(request.getMusicName());
        demon.setMusicId(request.getMusicId());
        demon.setMusicCreatorName(request.getMusicCreatorName());
        demon.setMusicUrl(request.getMusicUrl());
        demon.setRequirement(request.getRequirement());
        demon.setDifficulty(request.getDifficulty());
        return demon;
    }

    private DemonResponse toResponse(Demon demon) {
        DemonResponse response = new DemonResponse();
        response.setId(demon.getId());
        response.setLevelTitle(demon.getLevelTitle());
        response.setLevelId(demon.getLevelId());
        response.setCreatorName(demon.getCreatorName());
        response.setCreatorId(demon.getCreatorId());
        response.setDescription(demon.getDescription());
        response.setLevelPassword(demon.getLevelPassword());
        response.setMusicName(demon.getMusicName());
        response.setMusicId(demon.getMusicId());
        response.setMusicCreatorName(demon.getMusicCreatorName());
        response.setMusicUrl(demon.getMusicUrl());
        response.setRequirement(demon.getRequirement());
        response.setPosition(demon.getPosition());
        response.setPoints(demon.getPoints());
        response.setDifficulty(demon.getDifficulty());
        return response;
    }
}
