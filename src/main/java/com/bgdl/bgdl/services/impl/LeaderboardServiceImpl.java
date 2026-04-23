package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private final PlayerRepository playerRepository;
    private final RecordSubmissionRepository recordSubmissionRepository;

    @Override
    @Transactional
    public void rebuildLeaderboard() {
        List<Player> players = playerRepository.findAllByDeletedAtIsNull();
        List<RecordSubmission> acceptedSubmissions = recordSubmissionRepository.findAllByDeletedAtIsNullAndStatus(
                RecordSubmissionStatus.ACCEPTED
        );

        Map<UUID, List<Demon>> completedDemonsByPlayer = acceptedSubmissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getHolder().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toMap(
                                        submission -> submission.getDemon().getId(),
                                        RecordSubmission::getDemon,
                                        (left, right) -> left
                                ),
                                demons -> demons.values().stream()
                                        .filter(demon -> demon.getDeletedAt() == null)
                                        .sorted(Comparator.comparingInt(Demon::getPosition))
                                        .toList()
                        )
                ));

        for (Player player : players) {
            List<Demon> completedDemons = completedDemonsByPlayer.getOrDefault(player.getId(), List.of());
            player.setCompletedDemons(new LinkedHashSet<>(completedDemons));
            player.setHardestDemon(completedDemons.isEmpty() ? null : completedDemons.getFirst());
            player.setPoints(
                    completedDemons.stream()
                            .map(Demon::getPoints)
                            .mapToDouble(Double::doubleValue)
                            .sum()
            );
            player.setRank(null);
        }

        List<Player> rankedPlayers = players.stream()
                .filter(player -> !player.getCompletedDemons().isEmpty())
                .sorted(
                        Comparator.comparing(Player::getPoints, Comparator.reverseOrder())
                                .thenComparing(
                                        player -> player.getHardestDemon().getPosition()
                                )
                                .thenComparing(Player::getName, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(Player::getId)
                )
                .toList();

        for (int index = 0; index < rankedPlayers.size(); index++) {
            rankedPlayers.get(index).setRank(index + 1);
        }

        playerRepository.saveAll(players);
    }
}
