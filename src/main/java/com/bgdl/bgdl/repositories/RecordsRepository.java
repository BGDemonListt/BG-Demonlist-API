package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.RecordSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecordsRepository extends JpaRepository<RecordSubmission, UUID> {
}
