package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import com.bgdl.bgdl.models.entity.PlayerClaimRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerClaimRequestRepository extends JpaRepository<PlayerClaimRequest, UUID> {
    boolean existsByRequester_IdAndStatus(UUID requesterId, PlayerClaimStatus status);

    List<PlayerClaimRequest> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM PlayerClaimRequest c WHERE c.id = :id")
    Optional<PlayerClaimRequest> findByIdForUpdate(@Param("id") UUID id);
}
