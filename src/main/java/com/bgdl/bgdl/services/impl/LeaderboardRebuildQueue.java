package com.bgdl.bgdl.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardRebuildQueue {
    private static final Object TRANSACTION_REBUILD_MARKER = new Object();

    private final LeaderboardRebuildWorker leaderboardRebuildWorker;

    @Qualifier("leaderboardRebuildExecutor")
    private final Executor leaderboardRebuildExecutor;

    private final AtomicBoolean workerScheduled = new AtomicBoolean(false);
    private final Set<UUID> pendingDemonIds = new HashSet<>();
    private final Set<UUID> pendingPlayerIds = new HashSet<>();

    public void requestRebuild(Set<UUID> demonIds, Set<UUID> playerIds) {
        if (demonIds.isEmpty() && playerIds.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            registerAfterCommitRequest(demonIds, playerIds);
            return;
        }

        enqueueRebuild(demonIds, playerIds);
    }

    private void registerAfterCommitRequest(Set<UUID> demonIds, Set<UUID> playerIds) {
        PendingChanges changes = (PendingChanges) TransactionSynchronizationManager.getResource(TRANSACTION_REBUILD_MARKER);

        if (changes == null) {
            changes = new PendingChanges();
            TransactionSynchronizationManager.bindResource(TRANSACTION_REBUILD_MARKER, changes);
            PendingChanges committedChanges = changes;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enqueueRebuild(committedChanges.demonIds, committedChanges.playerIds);
                }

                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_REBUILD_MARKER);
                }
            });
        }

        changes.demonIds.addAll(demonIds);
        changes.playerIds.addAll(playerIds);
    }

    private void enqueueRebuild(Set<UUID> demonIds, Set<UUID> playerIds) {
        synchronized (this) {
            pendingDemonIds.addAll(demonIds);
            pendingPlayerIds.addAll(playerIds);
        }

        scheduleWorkerIfNeeded();
    }

    private void scheduleWorkerIfNeeded() {
        if (workerScheduled.compareAndSet(false, true)) {
            try {
                leaderboardRebuildExecutor.execute(this::drainQueue);
            } catch (RuntimeException exception) {
                workerScheduled.set(false);
                throw exception;
            }
        }
    }

    private void drainQueue() {
        boolean failed = false;

        try {
            while (true) {
                PendingChanges changes;
                synchronized (this) {
                    if (pendingDemonIds.isEmpty() && pendingPlayerIds.isEmpty()) {
                        break;
                    }
                    changes = new PendingChanges();
                    changes.demonIds.addAll(pendingDemonIds);
                    changes.playerIds.addAll(pendingPlayerIds);
                    pendingDemonIds.clear();
                    pendingPlayerIds.clear();
                }

                try {
                    leaderboardRebuildWorker.rebuildAffected(changes.demonIds, changes.playerIds);
                } catch (RuntimeException exception) {
                    log.error("Leaderboard rebuild failed; retrying", exception);
                    synchronized (this) {
                        pendingDemonIds.addAll(changes.demonIds);
                        pendingPlayerIds.addAll(changes.playerIds);
                    }
                    failed = true;
                    break;
                }
            }
        } finally {
            workerScheduled.set(false);
            if (failed) {
                CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS, leaderboardRebuildExecutor)
                        .execute(this::scheduleWorkerIfNeeded);
            } else if (hasPendingChanges()) {
                scheduleWorkerIfNeeded();
            }
        }
    }

    private synchronized boolean hasPendingChanges() {
        return !pendingDemonIds.isEmpty() || !pendingPlayerIds.isEmpty();
    }

    private static class PendingChanges {
        private final Set<UUID> demonIds = new HashSet<>();
        private final Set<UUID> playerIds = new HashSet<>();
    }
}
