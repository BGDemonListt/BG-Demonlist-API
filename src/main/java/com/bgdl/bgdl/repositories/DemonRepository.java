package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.Demon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemonRepository extends JpaRepository<Demon, UUID>, JpaSpecificationExecutor<Demon> {
    List<Demon> findAllByDeletedAtIsNullOrderByPositionAsc();

    Optional<Demon> findByDeletedAtIsNullAndLevelId(long levelId);

    Optional<Demon> findByDeletedAtIsNullAndLevelIdAndIdNot(long levelId, UUID excludedId);
}
