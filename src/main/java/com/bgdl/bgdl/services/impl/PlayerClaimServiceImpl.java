package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.exceptions.player.PlayerNotFoundException;
import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.PlayerClaimRequest;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.response.PlayerClaimRequestResponse;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.repositories.PlayerClaimRequestRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.PlayerClaimService;
import com.bgdl.bgdl.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerClaimServiceImpl implements PlayerClaimService {
    private final PlayerClaimRequestRepository claimRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public PlayerClaimRequestResponse requestClaim(UUID requesterId, UUID playerId) {
        User requester = userRepository.findActiveByIdForUpdate(requesterId)
                .orElseThrow(UserNotFoundException::new);
        if (!userService.canRequestPlayerClaim(requester)) {
            throw new BadRequestException("Player claims are unavailable after any record submission.");
        }
        if (claimRepository.existsByRequester_IdAndStatus(requesterId, PlayerClaimStatus.PENDING)) {
            throw new BadRequestException("You already have a pending player claim request.");
        }

        Player player = playerRepository.findActiveByIdForUpdate(playerId)
                .orElseThrow(PlayerNotFoundException::new);
        if (userRepository.existsByPlayer_Id(playerId)) {
            throw new BadRequestException("This player is already assigned to a user.");
        }

        PlayerClaimRequest claim = new PlayerClaimRequest();
        claim.setRequester(requester);
        claim.setPlayer(player);
        claim.setStatus(PlayerClaimStatus.PENDING);
        return toResponse(claimRepository.save(claim));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerClaimRequestResponse> getRequests() {
        return claimRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PlayerClaimRequestResponse decide(UUID requestId, UUID adminId, boolean accept) {
        PlayerClaimRequest claim = claimRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BadRequestException("Player claim request not found."));
        if (claim.getStatus() != PlayerClaimStatus.PENDING) {
            throw new BadRequestException("Only pending player claim requests can be reviewed.");
        }
        User admin = userRepository.findActiveByIdForUpdate(adminId).orElseThrow(UserNotFoundException::new);

        if (accept) {
            userService.assignPlayerToUser(claim.getRequester().getId(), claim.getPlayer().getId());
            claim.setStatus(PlayerClaimStatus.ACCEPTED);
        } else {
            claim.setStatus(PlayerClaimStatus.REJECTED);
        }
        claim.setReviewedBy(admin);
        claim.setReviewedAt(LocalDateTime.now());
        return toResponse(claimRepository.save(claim));
    }

    private PlayerClaimRequestResponse toResponse(PlayerClaimRequest claim) {
        Player player = claim.getPlayer();
        PlayerSummaryResponse playerResponse = new PlayerSummaryResponse();
        playerResponse.setId(player.getId());
        playerResponse.setName(player.getName());
        playerResponse.setPoints(player.getPoints());
        playerResponse.setPosition(player.getRank());

        PlayerClaimRequestResponse response = new PlayerClaimRequestResponse();
        response.setId(claim.getId());
        response.setRequesterId(claim.getRequester().getId());
        response.setRequesterName(claim.getRequester().getName());
        response.setPlayer(playerResponse);
        response.setStatus(claim.getStatus());
        response.setReviewedById(claim.getReviewedBy() == null ? null : claim.getReviewedBy().getId());
        response.setReviewedAt(claim.getReviewedAt());
        response.setCreatedAt(claim.getCreatedAt());
        return response;
    }
}
