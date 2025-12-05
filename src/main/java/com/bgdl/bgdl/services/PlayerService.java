package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PlayerResponse;

public interface PlayerService {
    PlayerResponse createPlayer(User user);
}
