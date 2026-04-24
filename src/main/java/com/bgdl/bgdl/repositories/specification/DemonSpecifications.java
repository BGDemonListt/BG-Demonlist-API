package com.bgdl.bgdl.repositories.specification;

import com.bgdl.bgdl.models.entity.Demon;
import org.springframework.data.jpa.domain.Specification;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DemonSpecifications {
    private DemonSpecifications() {
    }

    public static Specification<Demon> active() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<Demon> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        String normalizedName = "%" + name.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("levelTitle")), normalizedName);
    }

    public static Specification<Demon> hasAnySkillsetTag(Set<UUID> skillsetTagIds) {
        if (skillsetTagIds == null || skillsetTagIds.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        Set<UUID> normalizedTagIds = skillsetTagIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedTagIds.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return root.join("skillsetTags").get("id").in(normalizedTagIds);
        };
    }
}
