package com.bgdl.bgdl.integration;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.request.PlayerCreateRequest;
import com.bgdl.bgdl.models.request.RecordSubmissionRequest;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.services.DemonService;
import com.bgdl.bgdl.services.PlayerService;
import com.bgdl.bgdl.services.RecordSubmissionService;
import com.bgdl.bgdl.services.impl.DemonServiceImpl;
import com.bgdl.bgdl.services.impl.LeaderboardDatabaseLock;
import com.bgdl.bgdl.services.impl.LeaderboardRebuildQueue;
import com.bgdl.bgdl.services.impl.LeaderboardRebuildWorker;
import com.bgdl.bgdl.services.impl.LeaderboardServiceImpl;
import com.bgdl.bgdl.services.impl.LeaderboardStartupRepair;
import com.bgdl.bgdl.services.impl.PlayerServiceImpl;
import com.bgdl.bgdl.services.impl.RecordSubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(showSql = false, properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=30",
        "logging.level.net.ttddyy.dsproxy.listener=OFF"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PlayerServiceImpl.class, DemonServiceImpl.class, RecordSubmissionServiceImpl.class,
        LeaderboardServiceImpl.class, LeaderboardRebuildQueue.class, LeaderboardRebuildWorker.class,
        LeaderboardDatabaseLock.class, LeaderboardStartupRepair.class,
        LeaderboardPostgresIntegrationTest.ExecutorConfig.class})
class LeaderboardPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private PlayerService playerService;
    @Autowired private DemonService demonService;
    @Autowired private RecordSubmissionService recordSubmissionService;
    @Autowired private LeaderboardRebuildWorker rebuildWorker;
    @Autowired private LeaderboardStartupRepair startupRepair;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private DemonRepository demonRepository;
    @Autowired private RecordSubmissionRepository recordSubmissionRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PausedExecutor rebuildExecutor;

    @BeforeEach
    void cleanDatabase() {
        assertEquals(0, rebuildExecutor.queuedCount());
        jdbcTemplate.execute("TRUNCATE TABLE users, players, demons, record_submissions RESTART IDENTITY CASCADE");
    }

    @Test
    void committedAcceptanceRunsOnlyAfterCommitAndRollbackNeverSchedulesWork() throws Exception {
        Demon demon = createDemon();
        UUID playerId = createPlayer("Commit Player");
        UUID submissionId = createPending(playerId, demon.getId());

        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        assertThrows(RolledBack.class, () -> transaction.executeWithoutResult(ignored -> {
            accept(submissionId);
            assertEquals(0, rebuildExecutor.queuedCount());
            throw new RolledBack();
        }));
        assertEquals(RecordSubmissionStatus.PENDING, recordSubmissionRepository.findById(submissionId).orElseThrow().getStatus());
        assertEquals(0, rebuildExecutor.queuedCount());
        assertNull(playerRepository.findById(playerId).orElseThrow().getRank());

        accept(submissionId);
        assertEquals(1, rebuildExecutor.queuedCount());
        assertNull(playerRepository.findById(playerId).orElseThrow().getRank());
        rebuildExecutor.runNext();
        assertEquals(1, playerRepository.findById(playerId).orElseThrow().getRank());
        assertEquals(demon.getPoints(), playerRepository.findById(playerId).orElseThrow().getPoints());
    }

    @Test
    void multipleAcceptedRecordsInOneTransactionScheduleOneRebuild() throws Exception {
        Demon demon = createDemon();
        UUID first = createPlayer("First Victor");
        UUID second = createPlayer("Second Victor");
        UUID firstSubmission = createPending(first, demon.getId());
        UUID secondSubmission = createPending(second, demon.getId());

        new TransactionTemplate(transactionManager).executeWithoutResult(ignored -> {
            accept(firstSubmission);
            accept(secondSubmission);
            assertEquals(0, rebuildExecutor.queuedCount());
        });
        assertEquals(1, rebuildExecutor.queuedCount());
        rebuildExecutor.runNext();
        assertEquals(1, playerRepository.findById(first).orElseThrow().getRank());
        assertEquals(2, playerRepository.findById(second).orElseThrow().getRank());
    }

    @Test
    void concurrentAdminsCoalesceAndFinalRanksMatchAllCommittedDecisions() throws Exception {
        Demon demon = createDemon();
        List<UUID> players = new ArrayList<>();
        List<UUID> submissions = new ArrayList<>();
        for (int index = 0; index < 20; index++) {
            UUID playerId = createPlayer("Victor " + index);
            players.add(playerId);
            submissions.add(createPending(playerId, demon.getId()));
        }

        ExecutorService admins = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> decisions = new ArrayList<>();
            for (UUID submissionId : submissions) {
                decisions.add(admins.submit(() -> {
                    assertTrue(start.await(20, TimeUnit.SECONDS));
                    accept(submissionId);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> decision : decisions) {
                decision.get(30, TimeUnit.SECONDS);
            }

            assertEquals(1, rebuildExecutor.queuedCount());
            assertEquals(0, playerService.getPlayers(null, null, 1).getTotalElements());
            rebuildExecutor.runNext();

            List<Player> ranked = playerRepository.findAllByDeletedAtIsNullAndPointsGreaterThan(0);
            assertEquals(20, ranked.size());
            assertEquals(Set.copyOf(players), ranked.stream().map(Player::getId).collect(java.util.stream.Collectors.toSet()));
            assertEquals(java.util.stream.IntStream.rangeClosed(1, 20).boxed().collect(java.util.stream.Collectors.toSet()),
                    ranked.stream().map(Player::getRank).collect(java.util.stream.Collectors.toSet()));
            assertTrue(ranked.stream().allMatch(player -> player.getPoints().equals(demon.getPoints())));
            assertEquals(20, playerService.getPlayers(null, null, 1).getTotalElements());
        } finally {
            admins.shutdownNow();
        }
    }

    @Test
    void conflictingConcurrentDecisionsConvergeToTheCommittedStatus() throws Exception {
        Demon demon = createDemon();
        UUID playerId = createPlayer("Contested Player");
        UUID submissionId = createPending(playerId, demon.getId());

        ExecutorService admins = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> accept = admins.submit(() -> {
                assertTrue(start.await(20, TimeUnit.SECONDS));
                accept(submissionId);
                return null;
            });
            Future<?> reject = admins.submit(() -> {
                assertTrue(start.await(20, TimeUnit.SECONDS));
                changeStatus(submissionId, RecordSubmissionStatus.REJECTED);
                return null;
            });
            start.countDown();
            accept.get(30, TimeUnit.SECONDS);
            reject.get(30, TimeUnit.SECONDS);
            assertEquals(1, rebuildExecutor.queuedCount());
            rebuildExecutor.runNext();

            RecordSubmissionStatus finalStatus = recordSubmissionRepository.findById(submissionId).orElseThrow().getStatus();
            Player player = playerRepository.findById(playerId).orElseThrow();
            if (finalStatus == RecordSubmissionStatus.ACCEPTED) {
                assertEquals(1, player.getRank());
                assertEquals(demon.getPoints(), player.getPoints());
            } else {
                assertEquals(RecordSubmissionStatus.REJECTED, finalStatus);
                assertNull(player.getRank());
                assertEquals(0.0, player.getPoints());
            }
        } finally {
            admins.shutdownNow();
        }
    }

    @Test
    void rebuildWaitsForPostgresAdvisoryLockHeldByAnotherTransaction() throws Exception {
        Demon demon = createDemon();
        UUID playerId = createPlayer("Locked Player");
        UUID submissionId = createPending(playerId, demon.getId());
        accept(submissionId);
        assertEquals(1, rebuildExecutor.queuedCount());

        ExecutorService threads = Executors.newFixedThreadPool(2);
        CountDownLatch lockHeld = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CountDownLatch rebuildStarted = new CountDownLatch(1);
        try {
            Future<?> holder = threads.submit(() -> {
                new TransactionTemplate(transactionManager).executeWithoutResult(ignored -> {
                    jdbcTemplate.queryForObject("SELECT pg_advisory_xact_lock(81270416)", Object.class);
                    lockHeld.countDown();
                    try {
                        assertTrue(releaseLock.await(20, TimeUnit.SECONDS));
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                });
            });
            assertTrue(lockHeld.await(20, TimeUnit.SECONDS));
            Future<?> rebuild = threads.submit(() -> {
                rebuildStarted.countDown();
                rebuildExecutor.runNext();
                return null;
            });
            assertTrue(rebuildStarted.await(20, TimeUnit.SECONDS));
            assertThrows(java.util.concurrent.TimeoutException.class, () -> rebuild.get(200, TimeUnit.MILLISECONDS));
            assertNull(playerRepository.findById(playerId).orElseThrow().getRank());

            releaseLock.countDown();
            holder.get(20, TimeUnit.SECONDS);
            rebuild.get(30, TimeUnit.SECONDS);
            assertEquals(1, playerRepository.findById(playerId).orElseThrow().getRank());
        } finally {
            releaseLock.countDown();
            threads.shutdownNow();
        }
    }

    @Test
    void startupRepairRecoversCommittedRecordsWhoseQueueWorkWasLost() throws Exception {
        Demon demon = createDemon();
        UUID playerId = createPlayer("Recovered Player");
        UUID submissionId = createPending(playerId, demon.getId());
        RecordSubmission submission = recordSubmissionRepository.findById(submissionId).orElseThrow();
        submission.setStatus(RecordSubmissionStatus.ACCEPTED);
        recordSubmissionRepository.saveAndFlush(submission);

        assertEquals(0, rebuildExecutor.queuedCount());
        assertNull(playerRepository.findById(playerId).orElseThrow().getRank());
        startupRepair.repairAfterRestart();
        assertEquals(1, rebuildExecutor.queuedCount());
        rebuildExecutor.runNext();
        assertEquals(1, playerRepository.findById(playerId).orElseThrow().getRank());
    }

    @Test
    @EnabledIfSystemProperty(named = "bgdl.runLeaderboardLoadTest", matches = "true")
    void measuresThreeHundredReadersAndTwentyConcurrentAdminDecisions() throws Exception {
        Demon demon = createDemon();
        List<Player> players = new ArrayList<>();
        for (int index = 0; index < 300; index++) {
            Player player = new Player();
            player.setName("Load Player " + index);
            players.add(player);
        }
        List<RecordSubmission> submissions = new TransactionTemplate(transactionManager).execute(ignored -> {
            List<Player> savedPlayers = playerRepository.saveAll(players);
            Demon managedDemon = demonRepository.findById(demon.getId()).orElseThrow();
            List<RecordSubmission> records = new ArrayList<>();
            for (int index = 0; index < savedPlayers.size(); index++) {
                RecordSubmission record = new RecordSubmission();
                record.setHolder(savedPlayers.get(index));
                record.setDemon(managedDemon);
                record.setProgress(100);
                record.setYoutubeUrl("https://example.com/video");
                record.setRawFootageUrl("https://example.com/raw");
                record.setDescription("Load test completion");
                record.setStatus(index < 20 ? RecordSubmissionStatus.PENDING : RecordSubmissionStatus.ACCEPTED);
                records.add(record);
            }
            return recordSubmissionRepository.saveAll(records);
        });

        long initialStart = System.nanoTime();
        rebuildWorker.rebuildAffected(Set.of(demon.getId()), Set.of());
        long initialRebuildMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - initialStart);
        assertEquals(280, playerService.getPlayers(null, null, 1).getTotalElements());
        rebuildExecutor.enableAutomatic();

        ExecutorService readers = Executors.newFixedThreadPool(300);
        ExecutorService admins = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        Map<UUID, Long> committedAt = new java.util.concurrent.ConcurrentHashMap<>();
        try {
            List<Future<Long>> reads = new ArrayList<>();
            for (int index = 0; index < 300; index++) {
                reads.add(readers.submit(() -> {
                    assertTrue(start.await(30, TimeUnit.SECONDS));
                    long began = System.nanoTime();
                    playerService.getPlayers(null, null, 1);
                    return System.nanoTime() - began;
                }));
            }
            List<Future<?>> decisions = new ArrayList<>();
            for (int index = 0; index < 20; index++) {
                UUID submissionId = submissions.get(index).getId();
                UUID playerId = players.get(index).getId();
                decisions.add(admins.submit(() -> {
                    assertTrue(start.await(30, TimeUnit.SECONDS));
                    accept(submissionId);
                    committedAt.put(playerId, System.nanoTime());
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> decision : decisions) {
                decision.get(60, TimeUnit.SECONDS);
            }
            List<Long> readNanos = new ArrayList<>();
            for (Future<Long> read : reads) {
                readNanos.add(read.get(60, TimeUnit.SECONDS));
            }
            readNanos.sort(Comparator.naturalOrder());

            Map<UUID, Long> visibleAt = new HashMap<>();
            String placeholders = String.join(",", java.util.Collections.nCopies(20, "?"));
            String query = "SELECT id FROM players WHERE id IN (" + placeholders + ") AND rank IS NOT NULL";
            Object[] ids = players.subList(0, 20).stream().map(Player::getId).toArray();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
            while (visibleAt.size() < 20 && System.nanoTime() < deadline) {
                long observedAt = System.nanoTime();
                jdbcTemplate.query(query, result -> {
                    visibleAt.putIfAbsent((UUID) result.getObject(1), observedAt);
                }, ids);
                if (visibleAt.size() < 20) {
                    Thread.sleep(10);
                }
            }
            assertEquals(20, visibleAt.size(), "Accepted records did not reach the leaderboard within 60 seconds");
            assertEquals(300, playerService.getPlayers(null, null, 1).getTotalElements());

            List<Long> lagNanos = committedAt.entrySet().stream()
                    .map(entry -> visibleAt.get(entry.getKey()) - entry.getValue())
                    .sorted().toList();
            long latestCommit = committedAt.values().stream().mapToLong(Long::longValue).max().orElseThrow();
            long finalVisibility = visibleAt.values().stream().mapToLong(Long::longValue).max().orElseThrow();
            System.out.printf("LEADERBOARD_LOAD players=300 concurrentReads=300 concurrentAdmins=20 initialRebuildMs=%d "
                            + "workerDrains=%d totalDrainMs=%d maxDrainMs=%d readP95Ms=%d "
                            + "lagP50Ms=%d lagP95Ms=%d lagMaxMs=%d convergenceAfterLastCommitMs=%d%n",
                    initialRebuildMs,
                    rebuildExecutor.completedTasks.get(),
                    TimeUnit.NANOSECONDS.toMillis(rebuildExecutor.totalTaskNanos.get()),
                    TimeUnit.NANOSECONDS.toMillis(rebuildExecutor.maxTaskNanos.get()),
                    TimeUnit.NANOSECONDS.toMillis(readNanos.get(284)),
                    TimeUnit.NANOSECONDS.toMillis(lagNanos.get(9)),
                    TimeUnit.NANOSECONDS.toMillis(lagNanos.get(18)),
                    TimeUnit.NANOSECONDS.toMillis(lagNanos.get(19)),
                    TimeUnit.NANOSECONDS.toMillis(finalVisibility - latestCommit));
        } finally {
            readers.shutdownNow();
            admins.shutdownNow();
            rebuildExecutor.disableAutomatic();
        }
    }

    private Demon createDemon() {
        DemonRequest request = new DemonRequest();
        request.setLevelTitle("Postgres demon");
        request.setLevelId(1000);
        request.setCreatorName("Creator");
        request.setCreatorId(1);
        request.setDescription("Integration test demon");
        request.setLevelPassword("password");
        request.setMusicName("Track");
        request.setMusicId(1);
        request.setMusicCreatorName("Artist");
        request.setMusicUrl("https://example.com/music");
        request.setRequirement(100);
        request.setPosition(1);
        request.setDifficulty(DemonDifficulty.EXTREME);
        demonService.createDemon(request);
        return demonRepository.findByDeletedAtIsNullAndLevelId(1000L).orElseThrow();
    }

    private UUID createPlayer(String name) {
        PlayerCreateRequest request = new PlayerCreateRequest();
        request.setName(name);
        return playerService.createUnownedPlayer(request).getId();
    }

    private UUID createPending(UUID playerId, UUID demonId) {
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setPlayerId(playerId);
        request.setDemonId(demonId);
        request.setProgress(100);
        request.setYoutubeUrl("https://example.com/video");
        request.setRawFootageUrl("https://example.com/raw");
        request.setDescription("Completed demon");
        return recordSubmissionService.create(request).getId();
    }

    private void accept(UUID submissionId) {
        changeStatus(submissionId, RecordSubmissionStatus.ACCEPTED);
    }

    private void changeStatus(UUID submissionId, RecordSubmissionStatus status) {
        RecordSubmissionRequest request = new RecordSubmissionRequest();
        request.setId(submissionId);
        request.setStatus(status);
        recordSubmissionService.update(request);
    }

    private static final class RolledBack extends RuntimeException {
    }

    @TestConfiguration
    static class ExecutorConfig {
        @Bean(name = "leaderboardRebuildExecutor", destroyMethod = "shutdown")
        PausedExecutor leaderboardRebuildExecutor() {
            return new PausedExecutor();
        }
    }

    static final class PausedExecutor implements Executor {
        private final BlockingQueue<Runnable> queued = new LinkedBlockingQueue<>();
        private final ExecutorService worker = Executors.newSingleThreadExecutor();
        private final AtomicInteger completedTasks = new AtomicInteger();
        private final AtomicLong totalTaskNanos = new AtomicLong();
        private final AtomicLong maxTaskNanos = new AtomicLong();
        private volatile boolean automatic;

        @Override
        public void execute(Runnable command) {
            if (automatic) {
                worker.execute(() -> runMeasured(command));
            } else {
                queued.add(command);
            }
        }

        int queuedCount() {
            return queued.size();
        }

        void runNext() throws Exception {
            Runnable task = queued.poll(20, TimeUnit.SECONDS);
            assertFalse(task == null);
            worker.submit(() -> runMeasured(task)).get(30, TimeUnit.SECONDS);
        }

        void enableAutomatic() {
            automatic = true;
            Runnable task;
            while ((task = queued.poll()) != null) {
                execute(task);
            }
        }

        void disableAutomatic() throws Exception {
            worker.submit(() -> { }).get(30, TimeUnit.SECONDS);
            automatic = false;
        }

        private void runMeasured(Runnable task) {
            long began = System.nanoTime();
            try {
                task.run();
            } finally {
                long elapsed = System.nanoTime() - began;
                completedTasks.incrementAndGet();
                totalTaskNanos.addAndGet(elapsed);
                maxTaskNanos.accumulateAndGet(elapsed, Math::max);
            }
        }

        void shutdown() {
            worker.shutdownNow();
        }
    }
}
