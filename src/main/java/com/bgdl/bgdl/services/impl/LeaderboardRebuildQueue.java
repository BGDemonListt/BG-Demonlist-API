package com.bgdl.bgdl.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardRebuildQueue {
    private static final Object TRANSACTION_REBUILD_MARKER = new Object();

    private final LeaderboardRebuildWorker leaderboardRebuildWorker;

    @Qualifier("leaderboardRebuildExecutor")
    private final Executor leaderboardRebuildExecutor;

    private final AtomicBoolean rebuildRequested = new AtomicBoolean(false);
    private final AtomicBoolean workerScheduled = new AtomicBoolean(false);

    public void requestRebuild() {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            registerAfterCommitRequest();
            return;
        }

        enqueueRebuild();
    }

    private void registerAfterCommitRequest() {
        if (TransactionSynchronizationManager.getResource(TRANSACTION_REBUILD_MARKER) != null) {
            return;
        }

        TransactionSynchronizationManager.bindResource(TRANSACTION_REBUILD_MARKER, Boolean.TRUE);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enqueueRebuild();
            }

            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_REBUILD_MARKER);
            }
        });
    }

    private void enqueueRebuild() {
        rebuildRequested.set(true);
        scheduleWorkerIfNeeded();
    }

    private void scheduleWorkerIfNeeded() {
        if (!workerScheduled.compareAndSet(false, true)) {
            return;
        }

        leaderboardRebuildExecutor.execute(this::drainQueue);
    }

    private void drainQueue() {
        try {
            while (rebuildRequested.getAndSet(false)) {
                try {
                    leaderboardRebuildWorker.rebuildLeaderboard();
                } catch (RuntimeException exception) {
                    log.error("Leaderboard rebuild failed", exception);
                }
            }
        } finally {
            workerScheduled.set(false);

            if (rebuildRequested.get()) {
                scheduleWorkerIfNeeded();
            }
        }
    }
}
