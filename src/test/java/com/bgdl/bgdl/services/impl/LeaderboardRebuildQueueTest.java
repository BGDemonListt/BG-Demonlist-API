package com.bgdl.bgdl.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeaderboardRebuildQueueTest {

    @Mock
    private LeaderboardRebuildWorker leaderboardRebuildWorker;

    @Test
    void coalescesMultipleQueuedRequestsIntoOneRebuild() {
        Queue<Runnable> scheduledTasks = new ArrayDeque<>();
        Executor executor = scheduledTasks::add;
        LeaderboardRebuildQueue queue = new LeaderboardRebuildQueue(leaderboardRebuildWorker, executor);

        queue.requestRebuild();
        queue.requestRebuild();

        scheduledTasks.remove().run();

        verify(leaderboardRebuildWorker, times(1)).rebuildLeaderboard();
    }

    @Test
    void runsOneAdditionalRebuildWhenChangesArriveDuringExecution() {
        LeaderboardRebuildQueue[] queueHolder = new LeaderboardRebuildQueue[1];
        AtomicBoolean firstPass = new AtomicBoolean(true);
        Executor executor = Runnable::run;
        queueHolder[0] = new LeaderboardRebuildQueue(leaderboardRebuildWorker, executor);

        doAnswer(invocation -> {
            if (firstPass.compareAndSet(true, false)) {
                queueHolder[0].requestRebuild();
            }
            return null;
        }).when(leaderboardRebuildWorker).rebuildLeaderboard();

        queueHolder[0].requestRebuild();

        verify(leaderboardRebuildWorker, times(2)).rebuildLeaderboard();
    }

    @Test
    void coalescesFifteenConcurrentRequestsWhileARebuildIsAlreadyRunning() throws Exception {
        ExecutorService rebuildExecutor = Executors.newSingleThreadExecutor();
        ExecutorService callersExecutor = Executors.newFixedThreadPool(15);

        try {
            LeaderboardRebuildQueue queue = new LeaderboardRebuildQueue(leaderboardRebuildWorker, rebuildExecutor);
            CountDownLatch firstRebuildStarted = new CountDownLatch(1);
            CountDownLatch allowFirstRebuildToFinish = new CountDownLatch(1);
            CountDownLatch twoRebuildsObserved = new CountDownLatch(2);
            CountDownLatch releaseBurst = new CountDownLatch(1);
            CountDownLatch burstSubmitted = new CountDownLatch(15);

            doAnswer(invocation -> {
                twoRebuildsObserved.countDown();

                if (firstRebuildStarted.getCount() > 0) {
                    firstRebuildStarted.countDown();
                    assertTrue(allowFirstRebuildToFinish.await(5, TimeUnit.SECONDS));
                }

                return null;
            }).when(leaderboardRebuildWorker).rebuildLeaderboard();

            queue.requestRebuild();
            assertTrue(firstRebuildStarted.await(5, TimeUnit.SECONDS));

            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < 15; index++) {
                futures.add(callersExecutor.submit(() -> {
                    assertTrue(releaseBurst.await(5, TimeUnit.SECONDS));
                    queue.requestRebuild();
                    burstSubmitted.countDown();
                    return null;
                }));
            }

            releaseBurst.countDown();
            assertTrue(burstSubmitted.await(5, TimeUnit.SECONDS));
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            allowFirstRebuildToFinish.countDown();
            assertTrue(twoRebuildsObserved.await(5, TimeUnit.SECONDS));

            rebuildExecutor.shutdown();
            callersExecutor.shutdown();
            assertTrue(rebuildExecutor.awaitTermination(5, TimeUnit.SECONDS));
            assertTrue(callersExecutor.awaitTermination(5, TimeUnit.SECONDS));

            verify(leaderboardRebuildWorker, times(2)).rebuildLeaderboard();
        } finally {
            rebuildExecutor.shutdownNow();
            callersExecutor.shutdownNow();
        }
    }
}
