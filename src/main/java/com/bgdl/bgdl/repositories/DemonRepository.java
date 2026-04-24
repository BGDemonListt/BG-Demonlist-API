package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.Demon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemonRepository extends JpaRepository<Demon, UUID>, JpaSpecificationExecutor<Demon> {
    @Override
    @EntityGraph(attributePaths = "skillsetTags")
    Page<Demon> findAll(Specification<Demon> spec, Pageable pageable);

    @EntityGraph(attributePaths = "skillsetTags")
    List<Demon> findAllByDeletedAtIsNullOrderByPositionAsc();

    @EntityGraph(attributePaths = "skillsetTags")
    Optional<Demon> findByDeletedAtIsNullAndLevelId(long levelId);

    @EntityGraph(attributePaths = "skillsetTags")
    Optional<Demon> findByDeletedAtIsNullAndLevelIdAndIdNot(long levelId, UUID excludedId);

    @EntityGraph(attributePaths = "skillsetTags")
    List<Demon> findAllByDeletedAtIsNullAndSkillsetTags_Id(UUID skillsetTagId);
}
