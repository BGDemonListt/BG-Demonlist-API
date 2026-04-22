package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PlayerResponse;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.services.PlayerService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final ModelMapper modelMapper;

    @Override
    public PlayerResponse createPlayer(User user) {
        Player player = Player.builder()
                .name(user.getName())
                .user(user)
                .points(0.0)
                .completedDemons(Collections.emptySet())
                .build();

        Player savedUser = playerRepository.save(player);
        return modelMapper.map(savedUser, PlayerResponse.class);
    }

    @Override
    public Player getById(UUID id) {
        return getEntityById(id, true);
    }

    @Override
    public Optional<PlayerResponse> findClosestRelativePlayer(double oldPoints, double newPoints) {
        Optional<Player> optionalPlayer;

        if (newPoints > oldPoints) {
            // gained points → look above
            optionalPlayer = playerRepository
                    .findFirstByPointsGreaterThanOrderByPointsAsc(newPoints);
        } else {
            // lost points → look below
            optionalPlayer = playerRepository
                    .findFirstByPointsLessThanOrderByPointsDesc(newPoints);
        }

        return optionalPlayer.map(player -> modelMapper.map(player, PlayerResponse.class));
    }

    @Override
    public void shiftDownBetween(double newPoints, double oldPoints) {
        playerRepository.shiftDownBetween(newPoints, oldPoints);
    }

    @Override
    public void shiftUpBetween(double newPoints, double oldPoints) {
        playerRepository.shiftUpBetween(newPoints, oldPoints);
    }

    @Override
    public int getLastPosition() {
        return (int) playerRepository.countAllByDeletedAtIsNullAndRankIsNotNull();
    }

    @Override
    @Transactional
    public void beatDemon(Player player, Demon demon, int newPos) {
        Player fetchedPlayer = getEntityById(player.getId());

        if (fetchedPlayer.getCompletedDemons().contains(demon)) {
            return;
        }

        if (fetchedPlayer.getHardestDemon() == null || fetchedPlayer.getHardestDemon().getPosition() > demon.getPosition()) {
            fetchedPlayer.setHardestDemon(demon);
        }

        fetchedPlayer.getCompletedDemons().add(demon);
        fetchedPlayer.setPoints(fetchedPlayer.getPoints() + demon.getPoints());
        fetchedPlayer.setRank(newPos);
        playerRepository.save(fetchedPlayer);
    }

    @Override
    public void removeDemon(Player player, Demon demon, int newPos) {
        Player fetchedPlayer = getEntityById(player.getId());


        if (!fetchedPlayer.getCompletedDemons().contains(demon)) {
            return;
        }

        Set<Demon> completedDemons = fetchedPlayer.getCompletedDemons();
        completedDemons.remove(demon);

        if (!completedDemons.isEmpty()) {
            fetchedPlayer.setPoints(fetchedPlayer.getPoints() - demon.getPoints());
            fetchedPlayer.setRank(newPos);

            if (fetchedPlayer.getHardestDemon() != null && fetchedPlayer.getHardestDemon().equals(demon)) {
                Optional<Demon> hardestDemon = completedDemons.stream().max(Comparator.comparing(Demon::getPosition));
                hardestDemon.ifPresent(fetchedPlayer::setHardestDemon);
            }
        } else {
            fetchedPlayer.setHardestDemon(null);
            fetchedPlayer.setPoints(0.0);
            fetchedPlayer.setRank(null);
        }

        fetchedPlayer.setCompletedDemons(completedDemons);
        playerRepository.save(fetchedPlayer);
    }

    private Player getEntityById(UUID id) {
        Optional<Player> player = playerRepository.findById(id);

        if (player.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return player.get();
    }

    private Player getEntityById(UUID id, boolean deletedCheck) {
        Player player = getEntityById(id);

        if (deletedCheck && player.getDeletedAt() != null) {
            throw new IllegalArgumentException();
        }

        return player;
    }
}
