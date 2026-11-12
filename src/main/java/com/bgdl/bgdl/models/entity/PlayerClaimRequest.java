package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_claim_requests", indexes = {
        @Index(name = "idx_player_claim_status_created", columnList = "status, created_at"),
        @Index(name = "idx_player_claim_requester", columnList = "requester_id, status")
})
public class PlayerClaimRequest extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlayerClaimStatus status = PlayerClaimStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    private LocalDateTime reviewedAt;
}
