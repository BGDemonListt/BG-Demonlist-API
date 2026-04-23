package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByDeletedAtIsNullAndId(UUID id);

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
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY
              CASE WHEN p.rank IS NULL THEN 1 ELSE 0 END,
              p.rank ASC,
              p.points DESC,
              p.name ASC
            """)
    Page<Player> findLeaderboardPage(@Param("name") String name, Pageable pageable);

    List<Player> findAllByDeletedAtIsNull();

    long countByDeletedAtIsNull();
}
