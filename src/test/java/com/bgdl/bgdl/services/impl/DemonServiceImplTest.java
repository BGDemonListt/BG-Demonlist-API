package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.services.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemonServiceImplTest {

    @Mock
    private DemonRepository demonRepository;

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private DemonServiceImpl demonService;

    @Test
    void createDemonInsertsAtRequestedPositionAndRebuildsLeaderboard() {
        Demon existingFirst = demon("Existing First", 111L, 1, 323.0);
        Demon existingSecond = demon("Existing Second", 222L, 2, 150.0);
        DemonRequest request = demonRequest(333L, "Inserted", 1);

        when(demonRepository.findByDeletedAtIsNullAndLevelId(333L)).thenReturn(Optional.empty());
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(existingFirst, existingSecond)));
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
        verify(leaderboardService).rebuildLeaderboard();
    }

    @Test
    void updateDemonReordersExistingDemonAndRebuildsLeaderboard() {
        Demon first = demon("First", 111L, 1, 323.0);
        Demon second = demon("Second", 222L, 2, 150.0);
        Demon target = demon("Target", 333L, 3, 100.0);
        DemonRequest request = demonRequest(333L, "Target Updated", 1);

        when(demonRepository.findByDeletedAtIsNullAndLevelId(333L)).thenReturn(Optional.of(target));
        when(demonRepository.findByDeletedAtIsNullAndLevelIdAndIdNot(333L, target.getId())).thenReturn(Optional.empty());
        when(demonRepository.findAllByDeletedAtIsNullOrderByPositionAsc())
                .thenReturn(new ArrayList<>(List.of(first, second, target)));
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
        verify(leaderboardService).rebuildLeaderboard();
    }

    @Test
    void deleteSoftDeletesDemonRebalancesAndRebuildsLeaderboard() {
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
        verify(leaderboardService).rebuildLeaderboard();
    }

    private DemonRequest demonRequest(long levelId, String title, int position) {
        DemonRequest request = new DemonRequest();
        request.setLevelTitle(title);
        request.setLevelId(levelId);
        request.setCreatorName("Creator");
        request.setCreatorId(999L);
        request.setDescription("Description");
        request.setLevelPassword("copyable");
        request.setMusicName("Track");
        request.setMusicId(123L);
        request.setMusicCreatorName("Artist");
        request.setMusicUrl("https://example.com/track");
        request.setRequirement(100);
        request.setPosition(position);
        request.setDifficulty(DemonDifficulty.EXTREME);
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
        demon.setMusicName("Track");
        demon.setMusicId(1L);
        demon.setMusicCreatorName("Artist");
        demon.setMusicUrl("https://example.com");
        demon.setRequirement(100);
        demon.setPosition(position);
        demon.setPoints(points);
        demon.setDifficulty(DemonDifficulty.EXTREME);
        return demon;
    }
}
