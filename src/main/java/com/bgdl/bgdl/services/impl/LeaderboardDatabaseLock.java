package com.bgdl.bgdl.services.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardDatabaseLock {
    private final EntityManager entityManager;

    public void lock() {
        entityManager.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock(81270416)").getSingleResult();
    }
}
