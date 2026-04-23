package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.user.DiscordAccountAlreadyLinkedException;
import com.bgdl.bgdl.models.entity.DiscordProfile;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void linkDiscordAccountStoresDiscordSnapshotForCurrentUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        DiscordProfile discordProfile = DiscordProfile.builder()
                .id("123456789")
                .username("bgdl-user")
                .avatarUrl("https://cdn.discordapp.com/avatar.png")
                .linkedAt(LocalDateTime.now())
                .build();

        PublicUserResponse mappedResponse = new PublicUserResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByDiscord_Id(discordProfile.getId())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, PublicUserResponse.class)).thenReturn(mappedResponse);

        PublicUserResponse response = userService.linkDiscordAccount(userId, discordProfile);

        assertSame(mappedResponse, response);
        assertSame(discordProfile, user.getDiscord());
        verify(userRepository).save(user);
    }

    @Test
    void linkDiscordAccountRejectsDiscordProfilesAlreadyLinkedToAnotherUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        User existingLinkedUser = new User();
        existingLinkedUser.setId(UUID.randomUUID());

        DiscordProfile discordProfile = DiscordProfile.builder()
                .id("123456789")
                .username("bgdl-user")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByDiscord_Id(discordProfile.getId())).thenReturn(Optional.of(existingLinkedUser));

        assertThrows(DiscordAccountAlreadyLinkedException.class, () -> userService.linkDiscordAccount(userId, discordProfile));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unlinkDiscordAccountClearsDiscordSnapshot() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setDiscord(DiscordProfile.builder().id("123456789").username("bgdl-user").build());

        PublicUserResponse mappedResponse = new PublicUserResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, PublicUserResponse.class)).thenReturn(mappedResponse);

        PublicUserResponse response = userService.unlinkDiscordAccount(userId);

        assertSame(mappedResponse, response);
        assertNull(user.getDiscord());
        verify(userRepository).save(user);
    }
}
