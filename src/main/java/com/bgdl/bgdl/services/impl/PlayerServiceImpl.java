package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PlayerResponse;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.services.PlayerService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
                .completedDemons(new ArrayList<>())
                .build();

        Player savedUser = playerRepository.save(player);
        return modelMapper.map(savedUser, PlayerResponse.class);
    }
}
