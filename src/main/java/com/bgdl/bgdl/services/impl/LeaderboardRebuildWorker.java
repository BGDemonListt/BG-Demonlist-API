package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardRebuildWorker {
    private final PlayerRepository playerRepository;
    private final RecordSubmissionRepository recordSubmissionRepository;
    private final LeaderboardDatabaseLock databaseLock;

    @Transactional
    public void rebuildAffected(Set<UUID> demonIds, Set<UUID> playerIds) {
        // Serialize ranking updates across application instances without blocking API reads.
        databaseLock.lock();

        Set<UUID> affectedPlayerIds = new HashSet<>(playerIds);
        if (!demonIds.isEmpty()) {
            affectedPlayerIds.addAll(recordSubmissionRepository.findDistinctHolderIdsByStatusAndDemonIds(
                    RecordSubmissionStatus.ACCEPTED, demonIds
            ));
        }
        if (affectedPlayerIds.isEmpty()) {
            return;
        }

        List<Player> affectedPlayers = playerRepository.findAllByDeletedAtIsNullAndIdIn(affectedPlayerIds);
        if (affectedPlayers.isEmpty()) {
            return;
        }
        List<RecordSubmission> submissions = recordSubmissionRepository.findAllByDeletedAtIsNullAndStatusAndHolderIdIn(
                RecordSubmissionStatus.ACCEPTED, affectedPlayerIds
        );
        Map<UUID, Map<UUID, Demon>> completedByPlayer = new HashMap<>();
        for (RecordSubmission submission : submissions) {
            Demon demon = submission.getDemon();
            if (demon.getDeletedAt() == null) {
                completedByPlayer.computeIfAbsent(submission.getHolder().getId(), ignored -> new HashMap<>())
                        .putIfAbsent(demon.getId(), demon);
            }
        }

        for (Player player : affectedPlayers) {
            List<Demon> completed = completedByPlayer.getOrDefault(player.getId(), Map.of()).values().stream()
                    .sorted(Comparator.comparingInt(Demon::getPosition))
                    .toList();
            player.setCompletedDemons(new LinkedHashSet<>(completed));
            player.setHardestDemon(completed.isEmpty() ? null : completed.getFirst());
            player.setPoints(completed.stream().mapToDouble(Demon::getPoints).sum());
            if (completed.isEmpty()) {
                player.setRank(null);
            }
        }
        playerRepository.saveAll(affectedPlayers);
        playerRepository.flush();

        List<Player> rankedPlayers = new ArrayList<>(playerRepository.findAllByDeletedAtIsNullAndPointsGreaterThan(0.0));
        rankedPlayers.sort(
                Comparator.comparing(Player::getPoints, Comparator.reverseOrder())
                        .thenComparingInt(player -> player.getHardestDemon().getPosition())
                        .thenComparing(Player::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Player::getId)
        );

        List<Player> changedRanks = new ArrayList<>();
        for (int index = 0; index < rankedPlayers.size(); index++) {
            Player player = rankedPlayers.get(index);
            if (!Objects.equals(player.getRank(), index + 1)) {
                player.setRank(index + 1);
                changedRanks.add(player);
            }
        }
        playerRepository.saveAll(changedRanks);
    }
}
