package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecordSubmissionRepository extends JpaRepository<RecordSubmission, UUID> {
    Optional<RecordSubmission> findByDeletedAtIsNullAndId(UUID id);
    Optional<RecordSubmission> findByDeletedAtIsNullAndHolderIdAndDemonId(UUID holderId, UUID demonId);
    List<RecordSubmission> findAllByDeletedAtIsNull();
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatusAndHolder(RecordSubmissionStatus status, Player holder);
    List<RecordSubmission> findAllByDeletedAtIsNullAndStatusAndDemon(RecordSubmissionStatus status, Demon demon);
}
