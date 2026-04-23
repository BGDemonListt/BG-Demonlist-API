package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.PlayerDetailsResponse;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;

import java.util.UUID;

public interface PlayerService {
    void createPlayer(User user);
    Player getById(UUID id);
    PageResponse<PlayerSummaryResponse> getPlayers(String nameFilter, int page);
    PlayerDetailsResponse getPlayerDetails(UUID id);
}
