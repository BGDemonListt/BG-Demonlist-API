package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.exceptions.demon.DemonSkillsetTagLimitExceededException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.SkillsetTag;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.models.response.DemonSummaryResponse;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.SkillsetTagRepository;
import com.bgdl.bgdl.services.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemonServiceImplTest {

    @Mock
    private DemonRepository demonRepository;

    @Mock
    private LeaderboardService leaderboardService;

    @Mock
    private SkillsetTagRepository skillsetTagRepository;

    @InjectMocks
    private DemonServiceImpl demonService;

    @Test
    void getAllDemonsReturnsPagedSummariesFilteredByNameAndSkillsetTags() {
        SkillsetTag wave = skillsetTag("Wave");
        Demon first = demon("Acheron", 111L, 1, 323.0);
        first.setSkillsetTags(new LinkedHashSet<>(List.of(wave)));
        first.setYoutubeUrl("https://youtube.com/watch?v=acheron");
        Demon second = demon("Avernus", 222L, 2, 250.0);
        second.setYoutubeUrl("https://youtube.com/watch?v=avernus");

        when(demonRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 20), 2));

        PageResponse<DemonSummaryResponse> response = demonService.getAllDemons("ach", Set.of(wave.getId()), 1);

        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getContent().size());
        assertEquals(first.getId(), response.getContent().get(0).getId());
        assertEquals("Acheron", response.getContent().get(0).getName());
        assertEquals("Creator", response.getContent().get(0).getCreator());
        assertEquals("https://youtube.com/watch?v=acheron", response.getContent().get(0).getYoutubeUrl());
        assertEquals(1, response.getContent().get(0).getSkillsetTags().size());
        assertEquals("Wave", response.getContent().get(0).getSkillsetTags().get(0).getName());
    }

    @Test
    void createDemonInsertsAtRequestedPositionAndRequestsLeaderboardRebuild() {
        Demon existingFirst = demon("Existing First", 111L, 1, 323.0);
        Demon existingSecond = demon("Existing Second", 222L, 2, 150.0);
        SkillsetTag timing = skillsetTag("Timing");
        DemonRequest request = demonRequest(333L, "Inserted", 1, Set.of(timing.getId()));

        when(demonRepository.findByDeletedAtIsNullAndLevelId(333L)).thenReturn(Optional.empty());
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(existingFirst, existingSecond)));
        when(skillsetTagRepository.findAllByDeletedAtIsNullAndIdIn(anySet()))
                .thenReturn(List.of(timing));
        when(demonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        DemonResponse response = demonService.createDemon(request);

        ArgumentCaptor<List<Demon>> captor = ArgumentCaptor.forClass(List.class);
        verify(demonRepository).saveAll(captor.capture());
        List<Demon> savedDemons = captor.getValue();

        assertEquals("Inserted", savedDemons.get(0).getLevelTitle());
        assertEquals(1, savedDemons.get(0).getPosition());
        assertEquals(2, savedDemons.get(1).getPosition());
        assertEquals(3, savedDemons.get(2).getPosition());
        assertTrue(savedDemons.get(0).getPoints() > savedDemons.get(1).getPoints());
        assertTrue(savedDemons.get(1).getPoints() > savedDemons.get(2).getPoints());
        assertEquals("Inserted", response.getLevelTitle());
        assertEquals("https://youtube.com/watch?v=demon", response.getYoutubeUrl());
        assertEquals(1, response.getSkillsetTags().size());
        assertEquals("Timing", response.getSkillsetTags().get(0).getName());
        verify(leaderboardService).requestRebuild();
    }

    @Test
    void updateDemonReordersExistingDemonAndRequestsLeaderboardRebuild() {
        Demon first = demon("First", 111L, 1, 323.0);
        Demon second = demon("Second", 222L, 2, 150.0);
        Demon target = demon("Target", 333L, 3, 100.0);
        SkillsetTag nerveControl = skillsetTag("Nerve Control");
        DemonRequest request = demonRequest(333L, "Target Updated", 1, Set.of(nerveControl.getId()));

        when(demonRepository.findByDeletedAtIsNullAndLevelId(333L)).thenReturn(Optional.of(target));
        when(demonRepository.findByDeletedAtIsNullAndLevelIdAndIdNot(333L, target.getId())).thenReturn(Optional.empty());
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(first, second, target)));
        when(skillsetTagRepository.findAllByDeletedAtIsNullAndIdIn(anySet()))
                .thenReturn(List.of(nerveControl));
        when(demonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        DemonResponse response = demonService.update(333L, request);

        ArgumentCaptor<List<Demon>> captor = ArgumentCaptor.forClass(List.class);
        verify(demonRepository).saveAll(captor.capture());
        List<Demon> savedDemons = captor.getValue();

        assertEquals("Target Updated", savedDemons.get(0).getLevelTitle());
        assertEquals(1, savedDemons.get(0).getPosition());
        assertEquals(2, savedDemons.get(1).getPosition());
        assertEquals(3, savedDemons.get(2).getPosition());
        assertEquals("Target Updated", response.getLevelTitle());
        assertEquals("https://youtube.com/watch?v=demon", response.getYoutubeUrl());
        assertEquals(1, response.getSkillsetTags().size());
        assertEquals("Nerve Control", response.getSkillsetTags().get(0).getName());
        verify(leaderboardService).requestRebuild();
    }

    @Test
    void deleteSoftDeletesDemonRebalancesAndRequestsLeaderboardRebuild() {
        Demon toDelete = demon("Target", 333L, 2, 150.0);
        Demon remaining = demon("Remaining", 111L, 1, 323.0);

        when(demonRepository.findById(toDelete.getId())).thenReturn(Optional.of(toDelete));
        when(demonRepository.save(any(Demon.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(remaining)));
        when(demonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        demonService.delete(toDelete.getId());

        verify(demonRepository).save(toDelete);
        assertTrue(toDelete.getDeletedAt() != null);
        verify(demonRepository).saveAll(anyList());
        verify(leaderboardService).requestRebuild();
    }

    @Test
    void deleteRestoresDemonAtStoredPositionAndRequestsLeaderboardRebuild() {
        Demon restored = demon("Restored", 333L, 2, 150.0);
        restored.setDeletedAt(LocalDateTime.now());
        Demon remaining = demon("Remaining", 111L, 1, 323.0);

        when(demonRepository.findById(restored.getId())).thenReturn(Optional.of(restored));
        when(demonRepository.save(any(Demon.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(remaining, restored)));
        when(demonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        demonService.delete(restored.getId());

        ArgumentCaptor<List<Demon>> captor = ArgumentCaptor.forClass(List.class);
        verify(demonRepository).saveAll(captor.capture());
        List<Demon> savedDemons = captor.getValue();

        assertNull(restored.getDeletedAt());
        assertEquals(remaining.getId(), savedDemons.get(0).getId());
        assertEquals(restored.getId(), savedDemons.get(1).getId());
        assertEquals(1, savedDemons.get(0).getPosition());
        assertEquals(2, savedDemons.get(1).getPosition());
        verify(leaderboardService).requestRebuild();
    }

    @Test
    void createDemonRejectsMoreThanFourSkillsetTags() {
        DemonRequest request = demonRequest(
                333L,
                "Inserted",
                1,
                Set.of(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                )
        );

        when(demonRepository.findByDeletedAtIsNullAndLevelId(333L)).thenReturn(Optional.empty());

        assertThrows(DemonSkillsetTagLimitExceededException.class, () -> demonService.createDemon(request));
        verify(skillsetTagRepository, never()).findAllByDeletedAtIsNullAndIdIn(anySet());
    }

    private DemonRequest demonRequest(long levelId, String title, int position, Set<UUID> skillsetTagIds) {
        DemonRequest request = new DemonRequest();
        request.setLevelTitle(title);
        request.setLevelId(levelId);
        request.setCreatorName("Creator");
        request.setCreatorId(999L);
        request.setDescription("Description");
        request.setLevelPassword("copyable");
        request.setYoutubeUrl("https://youtube.com/watch?v=demon");
        request.setMusicName("Track");
        request.setMusicId(123L);
        request.setMusicCreatorName("Artist");
        request.setMusicUrl("https://example.com/track");
        request.setRequirement(100);
        request.setPosition(position);
        request.setDifficulty(DemonDifficulty.EXTREME);
        request.setSkillsetTagIds(new LinkedHashSet<>(skillsetTagIds));
        return request;
    }

    private Demon demon(String title, long levelId, int position, double points) {
        Demon demon = new Demon();
        demon.setId(UUID.randomUUID());
        demon.setLevelTitle(title);
        demon.setLevelId(levelId);
        demon.setCreatorName("Creator");
        demon.setCreatorId(1L);
        demon.setDescription("Description");
        demon.setLevelPassword("copyable");
        demon.setYoutubeUrl("https://youtube.com/watch?v=demon");
        demon.setMusicName("Track");
        demon.setMusicId(1L);
        demon.setMusicCreatorName("Artist");
        demon.setMusicUrl("https://example.com");
        demon.setRequirement(100);
        demon.setPosition(position);
        demon.setPoints(points);
        demon.setDifficulty(DemonDifficulty.EXTREME);
        demon.setSkillsetTags(new LinkedHashSet<>());
        return demon;
    }

    private SkillsetTag skillsetTag(String name) {
        SkillsetTag tag = new SkillsetTag();
        tag.setId(UUID.randomUUID());
        tag.setName(name);
        return tag;
    }
}
