package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;
import com.bgdl.bgdl.exceptions.demon.DemonCreateException;
import com.bgdl.bgdl.exceptions.demon.DemonInvalidPositionException;
import com.bgdl.bgdl.exceptions.demon.DemonNotFoundException;
import com.bgdl.bgdl.exceptions.demon.DemonSkillsetTagLimitExceededException;
import com.bgdl.bgdl.exceptions.skillsettag.SkillsetTagNotFoundException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.SkillsetTag;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonSummaryResponse;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.SkillsetTagRepository;
import com.bgdl.bgdl.repositories.specification.DemonSpecifications;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemonServiceImpl extends BaseService<Demon, UUID> implements DemonService {
    private static final int DEMONS_PAGE_SIZE = 20;

    private final DemonRepository demonRepository;
    private final SkillsetTagRepository skillsetTagRepository;
    private final LeaderboardService leaderboardService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DemonSummaryResponse> getAllDemons(String nameFilter, Set<UUID> skillsetTagIds, int page) {
        int sanitizedPage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(sanitizedPage - 1, DEMONS_PAGE_SIZE, Sort.by(Sort.Direction.ASC, "position"));
        Page<Demon> demonsPage = demonRepository.findAll(
                Specification.allOf(
                        DemonSpecifications.active(),
                        DemonSpecifications.nameContains(nameFilter),
                        DemonSpecifications.hasAnySkillsetTag(skillsetTagIds)
                ),
                pageable
        );

        return PageResponse.<DemonSummaryResponse>builder()
                .content(demonsPage.stream().map(this::toSummaryResponse).toList())
                .page(sanitizedPage)
                .size(demonsPage.getSize())
                .totalElements(demonsPage.getTotalElements())
                .totalPages(demonsPage.getTotalPages())
                .build();
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
        demon.setYoutubeUrl(request.getYoutubeUrl());
        demon.setMusicName(request.getMusicName());
        demon.setMusicId(request.getMusicId());
        demon.setMusicCreatorName(request.getMusicCreatorName());
        demon.setMusicUrl(request.getMusicUrl());
        demon.setRequirement(request.getRequirement());
        demon.setDifficulty(request.getDifficulty());
        demon.setSkillsetTags(resolveSkillsetTags(request.getSkillsetTagIds()));
        return demon;
    }

    private DemonSummaryResponse toSummaryResponse(Demon demon) {
        DemonSummaryResponse response = new DemonSummaryResponse();
        response.setId(demon.getId());
        response.setName(demon.getLevelTitle());
        response.setPosition(demon.getPosition());
        response.setPoints(demon.getPoints());
        response.setCreator(demon.getCreatorName());
        response.setYoutubeUrl(demon.getYoutubeUrl());
        response.setSkillsetTags(toSkillsetTagResponses(demon.getSkillsetTags()));
        return response;
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
        response.setYoutubeUrl(demon.getYoutubeUrl());
        response.setMusicName(demon.getMusicName());
        response.setMusicId(demon.getMusicId());
        response.setMusicCreatorName(demon.getMusicCreatorName());
        response.setMusicUrl(demon.getMusicUrl());
        response.setRequirement(demon.getRequirement());
        response.setPosition(demon.getPosition());
        response.setPoints(demon.getPoints());
        response.setDifficulty(demon.getDifficulty());
        response.setSkillsetTags(toSkillsetTagResponses(demon.getSkillsetTags()));
        return response;
    }

    private Set<SkillsetTag> resolveSkillsetTags(Set<UUID> skillsetTagIds) {
        if (skillsetTagIds == null || skillsetTagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        if (skillsetTagIds.size() > 4) {
            throw new DemonSkillsetTagLimitExceededException();
        }

        List<SkillsetTag> tags = skillsetTagRepository.findAllByDeletedAtIsNullAndIdIn(skillsetTagIds);
        if (tags.size() != skillsetTagIds.size()) {
            throw new SkillsetTagNotFoundException();
        }

        return tags.stream()
                .sorted(java.util.Comparator.comparing(SkillsetTag::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<SkillsetTagResponse> toSkillsetTagResponses(Collection<SkillsetTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        return tags.stream()
                .sorted(java.util.Comparator.comparing(SkillsetTag::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSkillsetTagResponse)
                .toList();
    }

    private SkillsetTagResponse toSkillsetTagResponse(SkillsetTag tag) {
        SkillsetTagResponse response = new SkillsetTagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        return response;
    }
}
