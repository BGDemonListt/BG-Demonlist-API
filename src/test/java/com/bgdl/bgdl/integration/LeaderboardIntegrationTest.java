package com.bgdl.bgdl.integration;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.enums.Role;
import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.exceptions.recordSubmissions.RecordSubmissionCreateException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.request.PlayerCreateRequest;
import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.models.response.PlayerSummaryResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.RecordSubmissionService;
import com.bgdl.bgdl.services.impl.DemonServiceImpl;
import com.bgdl.bgdl.services.impl.LeaderboardDatabaseLock;
import com.bgdl.bgdl.services.impl.LeaderboardRebuildWorker;
import com.bgdl.bgdl.services.impl.PlayerServiceImpl;
import com.bgdl.bgdl.services.impl.RecordSubmissionServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:leaderboard;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PlayerServiceImpl.class, DemonServiceImpl.class, RecordSubmissionServiceImpl.class,
        LeaderboardRebuildWorker.class})
class LeaderboardIntegrationTest {
    @Autowired private PlayerService playerService;
    @Autowired private DemonService demonService;
    @Autowired private RecordSubmissionService recordSubmissionService;
    @Autowired private LeaderboardRebuildWorker rebuildWorker;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private DemonRepository demonRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;
    @MockBean private LeaderboardService leaderboardService;
    @MockBean private LeaderboardDatabaseLock databaseLock;

    private final Set<UUID> queuedDemons = new HashSet<>();
    private final Set<UUID> queuedPlayers = new HashSet<>();
    private long nextLevelId;

    @BeforeEach
    void captureRebuildRequests() {
        nextLevelId = 1000;
        doAnswer(invocation -> {
            queuedDemons.addAll(invocation.getArgument(0));
            queuedPlayers.addAll(invocation.getArgument(1));
            return null;
        }).when(leaderboardService).requestRebuild(anySet(), anySet());
    }

    @Test
    void newUserAndAdminCreatedPlayersAreUnrankedUntilAnAcceptedCompletion() {
        UUID defaultPlayer = createDefaultPlayer("Logged In");
        UUID unownedPlayer = createUnownedPlayer("Admin Created", BulgarianRegion.SOFIA);

        assertUnranked(defaultPlayer);
        assertUnranked(unownedPlayer);
        assertTrue(leaderboard().isEmpty());
        assertFalse(hasQueuedRebuild());

        Demon demon = createDemon("First", 1);
        addAccepted(unownedPlayer, demon.getId());
        rebuildQueued();
        assertRank(unownedPlayer, 1, demon.getPoints());

        UUID laterLogin = createDefaultPlayer("Later Login");
        assertUnranked(laterLogin);
        assertEquals(List.of(unownedPlayer), leaderboard());
        assertEquals(1, playerService.getPlayers(null, null, 1).getTotalElements());
    }

    @Test
    void pendingRejectedAcceptedAndReversedStatusChangesRepositionOnlyScoredPlayers() {
        Demon top = createDemon("Top", 1);
        Demon lower = createDemon("Lower", 2);
        UUID alpha = createUnownedPlayer("Alpha", null);
        UUID beta = createUnownedPlayer("Beta", null);

        addAccepted(beta, lower.getId());
        rebuildQueued();
        UUID submission = addPending(alpha, top.getId());
        assertFalse(hasQueuedRebuild());
        assertUnranked(alpha);
        assertEquals(List.of(beta), leaderboard());

        changeStatus(submission, RecordSubmissionStatus.REJECTED);
        assertFalse(hasQueuedRebuild());
        assertEquals(List.of(beta), leaderboard());

        UUID unscoredSubmission = addPending(beta, top.getId());
        recordSubmissionService.delete(unscoredSubmission);
        assertFalse(hasQueuedRebuild());
        assertRank(beta, 1, lower.getPoints());
        recordSubmissionService.delete(unscoredSubmission);
        assertFalse(hasQueuedRebuild());

        changeStatus(submission, RecordSubmissionStatus.ACCEPTED);
        assertEquals(Set.of(alpha), queuedPlayers);
        rebuildQueued();
        assertEquals(List.of(alpha, beta), leaderboard());
        assertRank(alpha, 1, top.getPoints());
        assertRank(beta, 2, lower.getPoints());

        changeStatus(submission, RecordSubmissionStatus.ACCEPTED);
        assertFalse(hasQueuedRebuild());
        changeStatus(submission, RecordSubmissionStatus.PENDING);
        rebuildQueued();
        assertUnranked(alpha);
        assertRank(beta, 1, lower.getPoints());

        changeStatus(submission, RecordSubmissionStatus.REJECTED);
        assertFalse(hasQueuedRebuild());
        changeStatus(submission, RecordSubmissionStatus.ACCEPTED);
        rebuildQueued();
        assertEquals(List.of(alpha, beta), leaderboard());
    }

    @Test
    void manualAcceptedRecordsSumPointsAndSoftDeleteRestoresRanksAndHardestDemon() {
        Demon top = createDemon("Top", 1);
        Demon lower = createDemon("Lower", 2);
        UUID alpha = createUnownedPlayer("Alpha", null);
        UUID beta = createUnownedPlayer("Beta", null);
        addAccepted(beta, top.getId());
        UUID lowerRecord = addAccepted(alpha, lower.getId());
        rebuildQueued();
        assertEquals(List.of(beta, alpha), leaderboard());

        UUID topRecord = addAccepted(alpha, top.getId());
        rebuildQueued();
        assertEquals(List.of(alpha, beta), leaderboard());
        assertRank(alpha, 1, top.getPoints() + lower.getPoints());
        assertEquals(top.getId(), playerService.getPlayerDetails(alpha).getHardestDemon().getId());
        assertEquals(List.of(top.getId(), lower.getId()), playerService.getPlayerDetails(alpha)
                .getCompletedDemons().stream().map(d -> d.getId()).toList());

        changeStatus(lowerRecord, RecordSubmissionStatus.REJECTED);
        rebuildQueued();
        assertRank(alpha, 1, top.getPoints());
        assertEquals(List.of(top.getId()), playerService.getPlayerDetails(alpha).getCompletedDemons()
                .stream().map(d -> d.getId()).toList());
        changeStatus(lowerRecord, RecordSubmissionStatus.ACCEPTED);
        rebuildQueued();
        assertRank(alpha, 1, top.getPoints() + lower.getPoints());

        recordSubmissionService.delete(topRecord);
        rebuildQueued();
        assertEquals(List.of(beta, alpha), leaderboard());
        assertRank(alpha, 2, lower.getPoints());
        assertEquals(lower.getId(), playerService.getPlayerDetails(alpha).getHardestDemon().getId());

        recordSubmissionService.delete(lowerRecord);
        rebuildQueued();
        assertUnranked(alpha);
        assertEquals(List.of(beta), leaderboard());

        recordSubmissionService.delete(topRecord);
        rebuildQueued();
        assertRank(alpha, 1, top.getPoints());
        assertRank(beta, 2, top.getPoints());
        recordSubmissionService.delete(lowerRecord);
        rebuildQueued();
        assertRank(alpha, 1, top.getPoints() + lower.getPoints());
        assertEquals(List.of(alpha, beta), leaderboard());
    }

    @Test
    void movingDemonRecalculatesAffectedVictorsWithoutChangingUnmovedTopVictor() {
        Demon top = createDemon("Top", 1);
        Demon middle = createDemon("Middle", 2);
        Demon bottom = createDemon("Bottom", 3);
        UUID topVictor = createUnownedPlayer("Top Victor", null);
        UUID middleVictor = createUnownedPlayer("Middle Victor", null);
        UUID bottomVictor = createUnownedPlayer("Bottom Victor", null);
        addAccepted(topVictor, top.getId());
        addAccepted(middleVictor, middle.getId());
        addAccepted(bottomVictor, bottom.getId());
        rebuildQueued();

        double oldTopPoints = loadPlayer(topVictor).getPoints();
        double oldMiddlePoints = loadPlayer(middleVictor).getPoints();
        double oldBottomPoints = loadPlayer(bottomVictor).getPoints();
        assertEquals(List.of(topVictor, middleVictor, bottomVictor), leaderboard());

        demonService.update(bottom.getLevelId(), demonRequest("Bottom", bottom.getLevelId(), 2));
        assertEquals(Set.of(middle.getId(), bottom.getId()), queuedDemons);
        assertTrue(queuedPlayers.isEmpty());
        rebuildQueued();

        assertEquals(List.of(topVictor, bottomVictor, middleVictor), leaderboard());
        assertEquals(oldTopPoints, loadPlayer(topVictor).getPoints());
        assertEquals(oldMiddlePoints, loadPlayer(bottomVictor).getPoints());
        assertEquals(oldBottomPoints, loadPlayer(middleVictor).getPoints());
        assertEquals(1, loadPlayer(topVictor).getRank());
        assertEquals(2, loadPlayer(bottomVictor).getRank());
        assertEquals(3, loadPlayer(middleVictor).getRank());
    }

    @Test
    void addingAndDeletingDemonsRepricesVictorsAndExcludesDeletedCompletions() {
        Demon top = createDemon("Top", 1);
        Demon lower = createDemon("Lower", 2);
        UUID topVictor = createUnownedPlayer("Top Victor", null);
        UUID lowerVictor = createUnownedPlayer("Lower Victor", null);
        addAccepted(topVictor, top.getId());
        addAccepted(lowerVictor, lower.getId());
        rebuildQueued();
        double formerLowerPoints = loadPlayer(lowerVictor).getPoints();

        Demon inserted = createDemon("Inserted", 3);
        assertEquals(2, loadDemon(lower.getId()).getPosition());
        assertEquals(loadDemon(lower.getId()).getPoints(), loadPlayer(lowerVictor).getPoints());
        assertTrue(loadPlayer(lowerVictor).getPoints() > formerLowerPoints);
        assertRank(topVictor, 1, top.getPoints());

        addAccepted(topVictor, lower.getId());
        rebuildQueued();
        assertRank(topVictor, 1, top.getPoints() + loadDemon(lower.getId()).getPoints());

        demonService.delete(lower.getId());
        rebuildQueued();
        assertUnranked(lowerVictor);
        assertEquals(List.of(topVictor), leaderboard());
        assertTrue(playerService.getPlayerDetails(lowerVictor).getCompletedDemons().isEmpty());
        assertRank(topVictor, 1, top.getPoints());
        assertEquals(List.of(top.getId()), playerService.getPlayerDetails(topVictor).getCompletedDemons()
                .stream().map(d -> d.getId()).toList());

        demonService.delete(lower.getId());
        rebuildQueued();
        assertEquals(2, loadDemon(lower.getId()).getPosition());
        assertRank(lowerVictor, 2, loadDemon(lower.getId()).getPoints());
        assertRank(topVictor, 1, top.getPoints() + loadDemon(lower.getId()).getPoints());
        assertEquals(List.of(topVictor, lowerVictor), leaderboard());
        assertEquals(3, loadDemon(inserted.getId()).getPosition());
    }

    @Test
    void duplicateActiveRecordCannotAwardPointsTwice() {
        Demon demon = createDemon("Shared", 1);
        UUID player = createUnownedPlayer("Victor", null);
        addAccepted(player, demon.getId());
        rebuildQueued();

        assertThrows(RecordSubmissionCreateException.class, () -> addAccepted(player, demon.getId()));
        assertFalse(hasQueuedRebuild());
        assertRank(player, 1, demon.getPoints());
        assertEquals(List.of(demon.getId()), playerService.getPlayerDetails(player).getCompletedDemons()
                .stream().map(d -> d.getId()).toList());
    }

    @Test
    void equalPointsUseCaseInsensitiveNameThenUuidAndPagesExcludeUnrankedPlayers() {
        Demon demon = createDemon("Shared", 1);
        UUID alpha = createUnownedPlayer("alpha", BulgarianRegion.SOFIA);
        UUID upperAlpha = createUnownedPlayer("ALPHA", BulgarianRegion.SOFIA);
        UUID bravo = createUnownedPlayer("Bravo", BulgarianRegion.VARNA);
        UUID unranked = createUnownedPlayer("Aardvark", BulgarianRegion.SOFIA);
        addAccepted(alpha, demon.getId());
        addAccepted(upperAlpha, demon.getId());
        addAccepted(bravo, demon.getId());
        rebuildQueued();

        List<UUID> expected = new ArrayList<>(List.of(alpha, upperAlpha));
        expected.sort(UUID::compareTo);
        expected.add(bravo);
        assertEquals(expected, leaderboard());
        assertUnranked(unranked);
        assertEquals(3, playerService.getPlayers(null, null, 1).getTotalElements());
        assertEquals(2, playerService.getPlayers(null, BulgarianRegion.SOFIA, 1).getTotalElements());
        assertEquals(List.of(bravo), playerService.getPlayers("bra", null, 1).getContent().stream()
                .map(PlayerSummaryResponse::getId).toList());

        for (int i = 0; i < 14; i++) {
            UUID player = createUnownedPlayer("Player " + i, null);
            addAccepted(player, demon.getId());
        }
        rebuildQueued();
        assertEquals(17, playerService.getPlayers(null, null, 1).getTotalElements());
        assertEquals(15, playerService.getPlayers(null, null, 1).getContent().size());
        assertEquals(2, playerService.getPlayers(null, null, 2).getContent().size());
        assertEquals(2, playerService.getPlayers(null, null, 1).getTotalPages());
        assertEquals(17, playerService.getPlayers(null, null, 2).getContent().get(1).getPosition());
    }

    private UUID createDefaultPlayer(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setProvider(Provider.LOCAL);
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
        playerService.createPlayer(user);
        return user.getPlayer().getId();
    }

    private UUID createUnownedPlayer(String name, BulgarianRegion region) {
        PlayerCreateRequest request = new PlayerCreateRequest();
        request.setName(name);
        request.setRegion(region);
        return playerService.createUnownedPlayer(request).getId();
    }

    private Demon createDemon(String name, int position) {
        long levelId = nextLevelId++;
        demonService.createDemon(demonRequest(name, levelId, position));
        rebuildQueued();
        return demonRepository.findByDeletedAtIsNullAndLevelId(levelId).orElseThrow();
    }

    private DemonRequest demonRequest(String name, long levelId, int position) {
        DemonRequest request = new DemonRequest();
        request.setLevelTitle(name);
        request.setLevelId(levelId);
        request.setCreatorName("Creator");
        request.setCreatorId(1);
        request.setDescription("Test demon");
        request.setLevelPassword("password");
        request.setMusicName("Track");
        request.setMusicId(1);
        request.setMusicCreatorName("Artist");
        request.setMusicUrl("https://example.com/music");
        request.setRequirement(100);
        request.setPosition(position);
        request.setDifficulty(DemonDifficulty.EXTREME);
        return request;
    }

    private UUID addPending(UUID playerId, UUID demonId) {
        return recordSubmissionService.create(recordRequest(playerId, demonId)).getId();
    }

    private UUID addAccepted(UUID playerId, UUID demonId) {
        return recordSubmissionService.createAccepted(recordRequest(playerId, demonId)).getId();
    }

    private RecordSubmissionRequest recordRequest(UUID playerId, UUID demonId) {
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setPlayerId(playerId);
        request.setDemonId(demonId);
        request.setProgress(100);
        request.setYoutubeUrl("https://example.com/video");
        request.setRawFootageUrl("https://example.com/raw");
        request.setDescription("Completed test demon");
        return request;
    }

    private void changeStatus(UUID submissionId, RecordSubmissionStatus status) {
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setId(submissionId);
        request.setStatus(status);
        recordSubmissionService.update(request);
    }

    private void rebuildQueued() {
        if (hasQueuedRebuild()) {
            entityManager.flush();
            rebuildWorker.rebuildAffected(Set.copyOf(queuedDemons), Set.copyOf(queuedPlayers));
            queuedDemons.clear();
            queuedPlayers.clear();
            entityManager.flush();
            entityManager.clear();
        }
    }

    private boolean hasQueuedRebuild() {
        return !queuedDemons.isEmpty() || !queuedPlayers.isEmpty();
    }

    private Player loadPlayer(UUID id) {
        return playerRepository.findDetailedById(id).orElseThrow();
    }

    private Demon loadDemon(UUID id) {
        return demonRepository.findById(id).orElseThrow();
    }

    private List<UUID> leaderboard() {
        return playerService.getPlayers(null, null, 1).getContent().stream()
                .map(PlayerSummaryResponse::getId).toList();
    }

    private void assertUnranked(UUID id) {
        Player player = loadPlayer(id);
        assertEquals(0.0, player.getPoints());
        assertNull(player.getRank());
        assertNull(player.getHardestDemon());
        assertTrue(player.getCompletedDemons().isEmpty());
    }

    private void assertRank(UUID id, int rank, double points) {
        Player player = loadPlayer(id);
        assertEquals(rank, player.getRank());
        assertEquals(points, player.getPoints(), 0.0000001);
    }
}
