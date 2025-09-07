package com.bgdl.bgdl.repositories;

import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.models.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);

    List<VerificationToken> findByUserAndCreatedAtBefore(User user, LocalDateTime thresholdDateTime);

    void deleteAllByUser(User user);
}
