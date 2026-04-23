package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.player.PlayerNotFoundException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.PlayerDetailsResponse;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @Test
    void createPlayerCreatesAndLinksPlayerToUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Player One");

        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        playerService.createPlayer(user);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player createdPlayer = playerCaptor.getValue();

        assertEquals("Player One", createdPlayer.getName());
        assertEquals(0.0, createdPlayer.getPoints());
        assertNotNull(createdPlayer.getCompletedDemons());
        assertEquals(0, createdPlayer.getCompletedDemons().size());
        verify(userRepository).save(user);
        assertNotNull(user.getPlayer());
    }

    @Test
    void createPlayerDoesNothingWhenUserAlreadyHasPlayer() {
        User user = new User();
        user.setPlayer(new Player());

        playerService.createPlayer(user);

        verifyNoInteractions(playerRepository, userRepository);
    }

    @Test
    void getPlayersReturnsPagedSummaries() {
        Player first = player(UUID.randomUUID(), "Alpha", 400.0, 1);
        Player second = player(UUID.randomUUID(), "Beta", 250.5, 2);

        when(playerRepository.findLeaderboardPage(eq("alp"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 15), 2));

        PageResponse<PlayerSummaryResponse> response = playerService.getPlayers("alp", 1);

        assertEquals(1, response.getPage());
        assertEquals(15, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getContent().size());
        assertEquals("Alpha", response.getContent().get(0).getName());
        assertEquals(1, response.getContent().get(0).getPosition());
    }

    @Test
    void getPlayerDetailsReturnsSortedCompletedDemonsAndHardestDemon() {
        Demon hardest = demon("Hardest", 1001L, 1);
        Demon easier = demon("Easier", 1002L, 4);

        Player player = player(UUID.randomUUID(), "Player One", 500.0, 1);
        player.setHardestDemon(hardest);
        player.setCompletedDemons(new LinkedHashSet<>(List.of(easier, hardest)));

        when(playerRepository.findDetailedById(player.getId())).thenReturn(Optional.of(player));

        PlayerDetailsResponse response = playerService.getPlayerDetails(player.getId());

        assertEquals("Player One", response.getName());
        assertEquals(500.0, response.getPoints());
        assertEquals(1, response.getPosition());
        assertEquals("Hardest", response.getHardestDemon().getLevelTitle());
        assertEquals(2, response.getCompletedDemons().size());
        assertEquals("Hardest", response.getCompletedDemons().get(0).getLevelTitle());
        assertEquals("Easier", response.getCompletedDemons().get(1).getLevelTitle());
    }

    @Test
    void getPlayerDetailsThrowsWhenMissing() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findDetailedById(playerId)).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> playerService.getPlayerDetails(playerId));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findByDeletedAtIsNullAndId(playerId)).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> playerService.getById(playerId));
    }

    private Player player(UUID id, String name, double points, Integer rank) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setPoints(points);
        player.setRank(rank);
        player.setCompletedDemons(new LinkedHashSet<>());
        return player;
    }

    private Demon demon(String title, long levelId, int position) {
        Demon demon = new Demon();
        demon.setId(UUID.randomUUID());
        demon.setLevelTitle(title);
        demon.setLevelId(levelId);
        demon.setPosition(position);
        demon.setPoints(100.0);
        return demon;
    }
}
