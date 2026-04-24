package com.bgdl.bgdl.repositories.specification;

import com.bgdl.bgdl.models.entity.Demon;
import org.springframework.data.jpa.domain.Specification;

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
}
