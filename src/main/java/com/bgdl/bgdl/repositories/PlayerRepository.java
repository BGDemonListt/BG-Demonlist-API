package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.models.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByDeletedAtIsNullAndId(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Player p WHERE p.deletedAt IS NULL AND p.id = :id")
    Optional<Player> findActiveByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"hardestDemon", "completedDemons"})
    @Query("""
            SELECT p
            FROM Player p
            WHERE p.deletedAt IS NULL
              AND p.id = :id
            """)
    Optional<Player> findDetailedById(@Param("id") UUID id);

    @Query("""
            SELECT p
            FROM Player p
            WHERE p.deletedAt IS NULL
              AND p.rank IS NOT NULL
              AND p.points > 0
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
              AND (:region IS NULL OR p.region = :region)
            ORDER BY
              p.rank ASC,
              p.id ASC
            """)
    Page<Player> findLeaderboardPage(
            @Param("name") String name,
            @Param("region") BulgarianRegion region,
            Pageable pageable
    );

    List<Player> findAllByDeletedAtIsNull();

    @Query("SELECT p.id FROM Player p WHERE p.deletedAt IS NULL")
    Set<UUID> findActivePlayerIds();

    List<Player> findAllByDeletedAtIsNullAndIdIn(Set<UUID> ids);

    @EntityGraph(attributePaths = "hardestDemon")
    List<Player> findAllByDeletedAtIsNullAndPointsGreaterThan(double points);

    long countByDeletedAtIsNull();
}
