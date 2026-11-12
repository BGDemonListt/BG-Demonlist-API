package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.enums.Role;
import com.bgdl.bgdl.exceptions.common.AccessDeniedException;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.exceptions.user.DiscordAccountAlreadyLinkedException;
import com.bgdl.bgdl.models.entity.DiscordProfile;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.request.ProfileUpdateRequest;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private PlayerRepository playerRepository;

    @Mock
    private RecordSubmissionRepository recordSubmissionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateProfileUpdatesOnlyNameAndPlayerRegionForCurrentUser() {
        UUID userId = UUID.randomUUID();
        Player player = new Player();
        player.setName("Old Name");

        User user = new User();
        user.setId(userId);
        user.setName("Old Name");
        user.setEmail("old@example.com");
        user.setRole(Role.USER);
        user.setPlayer(player);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("  New Name  ");
        request.setRegion(BulgarianRegion.VARNA);

        PublicUserResponse currentUser = PublicUserResponse.builder()
                .id(userId)
                .role(Role.USER)
                .build();
        PublicUserResponse mappedResponse = new PublicUserResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, PublicUserResponse.class)).thenReturn(mappedResponse);

        PublicUserResponse response = userService.updateProfile(userId, request, currentUser);

        assertSame(mappedResponse, response);
        assertEquals("New Name", user.getName());
        assertEquals("New Name", player.getName());
        assertEquals(BulgarianRegion.VARNA, player.getRegion());
        assertEquals("old@example.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfileRejectsOtherUsersForNonAdmins() {
        UUID targetUserId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(targetUserId);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setRegion(BulgarianRegion.BURGAS);

        PublicUserResponse currentUser = PublicUserResponse.builder()
                .id(UUID.randomUUID())
                .role(Role.USER)
                .build();

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        assertThrows(AccessDeniedException.class, () -> userService.updateProfile(targetUserId, request, currentUser));
        verify(userRepository, never()).save(any(User.class));
    }

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

    @Test
    void canRequestPlayerClaimOnlyWhenPlayerHasNoActiveSubmissions() {
        Player player = new Player();
        player.setId(UUID.randomUUID());
        User user = new User();
        user.setPlayer(player);

        when(recordSubmissionRepository.countByDeletedAtIsNullAndHolder_Id(player.getId())).thenReturn(0L);
        assertEquals(true, userService.canRequestPlayerClaim(user));

        when(recordSubmissionRepository.countByDeletedAtIsNullAndHolder_Id(player.getId())).thenReturn(1L);
        assertEquals(false, userService.canRequestPlayerClaim(user));
        verify(recordSubmissionRepository, org.mockito.Mockito.times(2))
                .countByDeletedAtIsNullAndHolder_Id(player.getId());
    }

    @Test
    void userWithoutPlayerCannotRequestPlayerClaim() {
        assertEquals(false, userService.canRequestPlayerClaim(new User()));
        verify(recordSubmissionRepository, never()).countByDeletedAtIsNullAndHolder_Id(any());
    }

    @Test
    void adminPlayerAssignmentSwapsToUnownedTarget() {
        UUID userId = UUID.randomUUID();
        Player previousPlayer = new Player();
        previousPlayer.setId(UUID.randomUUID());
        Player targetPlayer = new Player();
        targetPlayer.setId(UUID.randomUUID());
        User user = new User();
        user.setId(userId);
        user.setPlayer(previousPlayer);

        when(userRepository.findActiveByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findActiveByIdForUpdate(targetPlayer.getId())).thenReturn(Optional.of(targetPlayer));
        when(userRepository.existsByPlayer_Id(targetPlayer.getId())).thenReturn(false);

        userService.assignPlayerToUser(userId, targetPlayer.getId());

        assertSame(targetPlayer, user.getPlayer());
        verify(userRepository).save(user);
    }

    @Test
    void adminPlayerAssignmentRejectsAlreadyOwnedTarget() {
        UUID userId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Player targetPlayer = new Player();
        targetPlayer.setId(playerId);
        when(userRepository.findActiveByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findActiveByIdForUpdate(playerId)).thenReturn(Optional.of(targetPlayer));
        when(userRepository.existsByPlayer_Id(playerId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.assignPlayerToUser(userId, playerId));
        verify(userRepository, never()).save(any(User.class));
    }
}
