package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.PlayerClaimRequest;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.PlayerClaimRequestRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerClaimServiceImplTest {
    @Mock
    private PlayerClaimRequestRepository claimRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private PlayerClaimServiceImpl service;

    @Test
    void requestClaimCreatesPendingRequestForEligibleUserAndUnownedPlayer() {
        UUID userId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        User requester = user(userId);
        Player target = player(playerId, "Claimable");
        when(userRepository.findActiveByIdForUpdate(userId)).thenReturn(Optional.of(requester));
        when(userService.canRequestPlayerClaim(requester)).thenReturn(true);
        when(claimRepository.existsByRequester_IdAndStatus(userId, PlayerClaimStatus.PENDING)).thenReturn(false);
        when(playerRepository.findActiveByIdForUpdate(playerId)).thenReturn(Optional.of(target));
        when(userRepository.existsByPlayer_Id(playerId)).thenReturn(false);
        when(claimRepository.save(any(PlayerClaimRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.requestClaim(userId, playerId);

        assertEquals(PlayerClaimStatus.PENDING, response.getStatus());
        assertEquals(userId, response.getRequesterId());
        assertEquals(playerId, response.getPlayer().getId());
    }

    @Test
    void requestClaimRejectsUserWithAnyRecordSubmission() {
        UUID userId = UUID.randomUUID();
        User requester = user(userId);
        when(userRepository.findActiveByIdForUpdate(userId)).thenReturn(Optional.of(requester));
        when(userService.canRequestPlayerClaim(requester)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.requestClaim(userId, UUID.randomUUID()));
        verify(playerRepository, never()).findActiveByIdForUpdate(any());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void requestClaimRejectsWhenAnotherRequestIsAlreadyPending() {
        UUID userId = UUID.randomUUID();
        User requester = user(userId);
        when(userRepository.findActiveByIdForUpdate(userId)).thenReturn(Optional.of(requester));
        when(userService.canRequestPlayerClaim(requester)).thenReturn(true);
        when(claimRepository.existsByRequester_IdAndStatus(userId, PlayerClaimStatus.PENDING)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.requestClaim(userId, UUID.randomUUID()));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void acceptedClaimAssignsPlayerAndRecordsReviewer() {
        UUID requestId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User requester = user(UUID.randomUUID());
        User admin = user(adminId);
        Player target = player(UUID.randomUUID(), "Claimable");
        PlayerClaimRequest claim = claim(requester, target);
        when(claimRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(claim));
        when(userRepository.findActiveByIdForUpdate(adminId)).thenReturn(Optional.of(admin));
        when(claimRepository.save(claim)).thenReturn(claim);

        var response = service.decide(requestId, adminId, true);

        assertEquals(PlayerClaimStatus.ACCEPTED, response.getStatus());
        assertEquals(adminId, response.getReviewedById());
        verify(userService).assignPlayerToUser(requester.getId(), target.getId());
    }

    @Test
    void rejectedClaimDoesNotChangePlayerOwnership() {
        UUID requestId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User requester = user(UUID.randomUUID());
        User admin = user(adminId);
        PlayerClaimRequest claim = claim(requester, player(UUID.randomUUID(), "Claimable"));
        when(claimRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(claim));
        when(userRepository.findActiveByIdForUpdate(adminId)).thenReturn(Optional.of(admin));
        when(claimRepository.save(claim)).thenReturn(claim);

        var response = service.decide(requestId, adminId, false);

        assertEquals(PlayerClaimStatus.REJECTED, response.getStatus());
        verify(userService, never()).assignPlayerToUser(any(), any());
    }

    private PlayerClaimRequest claim(User requester, Player player) {
        PlayerClaimRequest claim = new PlayerClaimRequest();
        claim.setId(UUID.randomUUID());
        claim.setRequester(requester);
        claim.setPlayer(player);
        claim.setStatus(PlayerClaimStatus.PENDING);
        return claim;
    }

    private User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        return user;
    }

    private Player player(UUID id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setPoints(0.0);
        return player;
    }
}
