package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.request.PlayerClaimRequestCreate;
import com.bgdl.bgdl.models.request.PlayerClaimDecisionRequest;
import com.bgdl.bgdl.models.response.PlayerClaimRequestResponse;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.security.filter.JwtAuthenticationFilter;
import com.bgdl.bgdl.services.PlayerClaimService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/player-claims")
@RequiredArgsConstructor
public class PlayerClaimController {
    private final PlayerClaimService playerClaimService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PlayerClaimRequestResponse> requestClaim(
            @Valid @RequestBody PlayerClaimRequestCreate request,
            HttpServletRequest httpRequest
    ) {
        PublicUserResponse user = (PublicUserResponse) httpRequest.getAttribute(JwtAuthenticationFilter.USER_KEY);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerClaimService.requestClaim(user.getId(), request.getPlayerId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PlayerClaimRequestResponse>> getRequests() {
        return ResponseEntity.ok(playerClaimService.getRequests());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerClaimRequestResponse> decide(
            @PathVariable UUID id,
            @Valid @RequestBody PlayerClaimDecisionRequest request,
            HttpServletRequest httpRequest
    ) {
        PublicUserResponse admin = (PublicUserResponse) httpRequest.getAttribute(JwtAuthenticationFilter.USER_KEY);
        if (request.getStatus() != com.bgdl.bgdl.enums.PlayerClaimStatus.ACCEPTED
                && request.getStatus() != com.bgdl.bgdl.enums.PlayerClaimStatus.REJECTED) {
            throw new BadRequestException("Decision status must be ACCEPTED or REJECTED.");
        }
        return ResponseEntity.ok(playerClaimService.decide(id, admin.getId(),
                request.getStatus() == com.bgdl.bgdl.enums.PlayerClaimStatus.ACCEPTED));
    }
}
