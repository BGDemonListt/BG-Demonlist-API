package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDiscord_Id(String discordId);

    List<User> findByEnabledFalseAndCreatedAtBeforeAndDeletedAtIsNull(LocalDateTime thresholdDateTime);
}
