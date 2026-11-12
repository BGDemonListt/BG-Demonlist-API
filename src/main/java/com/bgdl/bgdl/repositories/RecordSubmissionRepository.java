package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RecordSubmissionRepository extends JpaRepository<RecordSubmission, UUID> {
    Optional<RecordSubmission> findByDeletedAtIsNullAndId(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecordSubmission r WHERE r.id = :id")
    Optional<RecordSubmission> findByIdForUpdate(@Param("id") UUID id);
    Optional<RecordSubmission> findByDeletedAtIsNullAndHolderIdAndDemonId(UUID holderId, UUID demonId);
    List<RecordSubmission> findAllByDeletedAtIsNull();
    @EntityGraph(attributePaths = {"holder", "demon"})
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatus(RecordSubmissionStatus status);
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatusAndHolder(RecordSubmissionStatus status, Player holder);
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatusAndDemon(RecordSubmissionStatus status, Demon demon);

    @Query("""
            SELECT DISTINCT r.holder.id FROM RecordSubmission r
            WHERE r.deletedAt IS NULL AND r.status = :status AND r.demon.id IN :demonIds
            """)
    Set<UUID> findDistinctHolderIdsByStatusAndDemonIds(
            @Param("status") RecordSubmissionStatus status,
            @Param("demonIds") Set<UUID> demonIds
    );

    @EntityGraph(attributePaths = {"holder", "demon"})
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatusAndHolderIdIn(
            RecordSubmissionStatus status, Set<UUID> holderIds
    );
}
