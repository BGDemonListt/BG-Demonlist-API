package com.bgdl.bgdl.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecordsRepository extends JpaRepository<Record, UUID> {
}
