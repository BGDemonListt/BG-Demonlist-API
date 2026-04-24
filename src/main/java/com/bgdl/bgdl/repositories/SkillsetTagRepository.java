package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.SkillsetTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillsetTagRepository extends JpaRepository<SkillsetTag, UUID> {
    List<SkillsetTag> findAllByDeletedAtIsNullOrderByNameAsc();

    List<SkillsetTag> findAllByDeletedAtIsNullAndIdIn(Collection<UUID> ids);

    Optional<SkillsetTag> findByDeletedAtIsNullAndId(UUID id);

    Optional<SkillsetTag> findByDeletedAtIsNullAndNameIgnoreCase(String name);

    Optional<SkillsetTag> findByDeletedAtIsNullAndNameIgnoreCaseAndIdNot(String name, UUID excludedId);
}
