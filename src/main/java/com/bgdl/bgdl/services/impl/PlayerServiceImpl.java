package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.exceptions.player.PlayerNotFoundException;
import com.bgdl.bgdl.models.dto.DemonBaseDTO;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.PlayerDetailsResponse;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.models.response.RegionResponse;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private static final int PLAYERS_PAGE_SIZE = 15;

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createPlayer(User user) {
        if (user.getPlayer() != null) {
            return;
        }

        Player player = Player.builder()
                .name(user.getName())
                .points(0.0)
                .completedDemons(new LinkedHashSet<>())
                .build();

        Player savedPlayer = playerRepository.save(player);
        user.setPlayer(savedPlayer);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Player getById(UUID id) {
        return playerRepository.findByDeletedAtIsNullAndId(id)
                .orElseThrow(PlayerNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlayerSummaryResponse> getPlayers(String nameFilter, BulgarianRegion regionFilter, int page) {
        int sanitizedPage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(sanitizedPage - 1, PLAYERS_PAGE_SIZE);
        Page<Player> playersPage = playerRepository.findLeaderboardPage(
                nameFilter == null ? "" : nameFilter.trim(),
                regionFilter,
                pageable
        );

        return PageResponse.<PlayerSummaryResponse>builder()
                .content(playersPage.stream().map(this::toPlayerSummary).toList())
                .page(sanitizedPage)
                .size(playersPage.getSize())
                .totalElements(playersPage.getTotalElements())
                .totalPages(playersPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerDetailsResponse getPlayerDetails(UUID id) {
        Player player = playerRepository.findDetailedById(id)
                .orElseThrow(PlayerNotFoundException::new);

        PlayerDetailsResponse response = new PlayerDetailsResponse();
        response.setId(player.getId());
        response.setName(player.getName());
        response.setRegion(toRegionResponse(player.getRegion()));
        response.setPoints(player.getPoints());
        response.setPosition(player.getRank());
        response.setHardestDemon(toDemonBase(player.getHardestDemon()));
        response.setCompletedDemons(
                player.getCompletedDemons().stream()
                        .sorted(Comparator.comparingInt(Demon::getPosition))
                        .map(this::toDemonBase)
                        .toList()
        );
        return response;
    }

    @Override
    public List<RegionResponse> getAvailableRegions() {
        return BulgarianRegion.orderedValues()
                .stream()
                .map(RegionResponse::from)
                .toList();
    }

    private PlayerSummaryResponse toPlayerSummary(Player player) {
        PlayerSummaryResponse response = new PlayerSummaryResponse();
        response.setId(player.getId());
        response.setName(player.getName());
        response.setPoints(player.getPoints());
        response.setPosition(player.getRank());
        return response;
    }

    private DemonBaseDTO toDemonBase(Demon demon) {
        if (demon == null) {
            return null;
        }

        DemonBaseDTO response = new DemonBaseDTO();
        response.setId(demon.getId());
        response.setLevelTitle(demon.getLevelTitle());
        response.setLevelId(demon.getLevelId());
        return response;
    }

    private RegionResponse toRegionResponse(BulgarianRegion region) {
        if (region == null) {
            return null;
        }

        return RegionResponse.from(region);
    }
}
