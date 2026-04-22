package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    // closest above
    Optional<Player> findFirstByPointsGreaterThanOrderByPointsAsc(double points);

    // closest below
    Optional<Player> findFirstByPointsLessThanOrderByPointsDesc(double points);

    // On points gain
    @Modifying
    @Query("UPDATE Player p SET p.rank = p.rank + 1 " +
            "WHERE p.points >= :oldPoints AND p.points <= :newPoints")
    void shiftDownBetween(double newPoints, double oldPoints);

    // On points lose
    @Modifying
    @Query("UPDATE Player p SET p.rank = p.rank - 1 " +
            "WHERE p.points <= :oldPoints AND p.points >= :newPoints")
    void shiftUpBetween(double newPoints, double oldPoints);

    long countAllByDeletedAtIsNullAndRankIsNotNull();
}
