package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PlayerResponse;

import java.util.Optional;
import java.util.UUID;

public interface PlayerService {
    PlayerResponse createPlayer(User user);
    Player getById(UUID id);
    Optional<PlayerResponse> findClosestRelativePlayer(double oldPoints, double newPoints);
    void shiftDownBetween(double newPoints, double oldPoints);
    void shiftUpBetween(double newPoints, double oldPoints);
    int getLastPosition();
    void beatDemon(Player player, Demon demon, int newPos);
    void removeDemon(Player player, Demon demon, int newPos);
}
