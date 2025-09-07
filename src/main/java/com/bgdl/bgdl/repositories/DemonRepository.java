package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.Demon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DemonRepository extends JpaRepository<Demon, UUID> {
    List<Demon> findByDeletedAtIsNullOrderByPositionAsc();
}
