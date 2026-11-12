package com.bgdl.bgdl.integration;

import com.bgdl.bgdl.enums.PlayerClaimStatus;
import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.RecordSubmissionStatus;
import com.bgdl.bgdl.enums.Role;
import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.entity.RecordSubmission;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.PlayerClaimRequestRepository;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.PlayerRepository;
import com.bgdl.bgdl.repositories.RecordSubmissionRepository;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.PlayerClaimService;
import com.bgdl.bgdl.services.UserService;
import com.bgdl.bgdl.services.LeaderboardService;
import com.bgdl.bgdl.services.impl.PlayerClaimServiceImpl;
import com.bgdl.bgdl.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:player-claims;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PlayerClaimServiceImpl.class, UserServiceImpl.class})
class PlayerClaimIntegrationTest {
    @Autowired
    private PlayerClaimService playerClaimService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecordSubmissionRepository recordSubmissionRepository;
    @Autowired
    private DemonRepository demonRepository;
    @Autowired
    private PlayerClaimRequestRepository claimRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private LeaderboardService leaderboardService;

    @Test
    void acceptingClaimPersistsNewOwnerAndDetachesPreviousPlayer() {
        Player previousPlayer = playerRepository.saveAndFlush(player("Default Player"));
        Player claimedPlayer = playerRepository.saveAndFlush(player("Claimed Player"));
        User requester = userRepository.saveAndFlush(user("claimant@example.com", Role.USER, previousPlayer));
        User admin = userRepository.saveAndFlush(user("admin@example.com", Role.ADMIN, null));

        var request = playerClaimService.requestClaim(requester.getId(), claimedPlayer.getId());
        var result = playerClaimService.decide(request.getId(), admin.getId(), true);

        assertEquals(PlayerClaimStatus.ACCEPTED, result.getStatus());
        assertEquals(admin.getId(), result.getReviewedById());
        assertNotNull(result.getReviewedAt());
        assertEquals(claimedPlayer.getId(), userRepository.findById(requester.getId()).orElseThrow()
                .getPlayer().getId());
        assertFalse(userRepository.existsByPlayer_Id(previousPlayer.getId()));
        assertTrue(userRepository.existsByPlayer_Id(claimedPlayer.getId()));
        assertEquals(PlayerClaimStatus.ACCEPTED, claimRepository.findById(request.getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectedClaimLeavesCurrentPlayerAttachedAndTargetUnowned() {
        Player currentPlayer = playerRepository.saveAndFlush(player("Current Player"));
        Player targetPlayer = playerRepository.saveAndFlush(player("Target Player"));
        User requester = userRepository.saveAndFlush(user("reject@example.com", Role.USER, currentPlayer));
        User admin = userRepository.saveAndFlush(user("reviewer@example.com", Role.ADMIN, null));

        var request = playerClaimService.requestClaim(requester.getId(), targetPlayer.getId());
        var result = playerClaimService.decide(request.getId(), admin.getId(), false);

        assertEquals(PlayerClaimStatus.REJECTED, result.getStatus());
        assertEquals(currentPlayer.getId(), userRepository.findById(requester.getId()).orElseThrow()
                .getPlayer().getId());
        assertTrue(userRepository.existsByPlayer_Id(currentPlayer.getId()));
        assertFalse(userRepository.existsByPlayer_Id(targetPlayer.getId()));
    }

    @Test
    void anyRecordSubmissionStatusMakesClaimIneligible() {
        Demon demon = recordDemon();
        for (RecordSubmissionStatus status : RecordSubmissionStatus.values()) {
            Player currentPlayer = playerRepository.saveAndFlush(player("Current " + status));
            Player targetPlayer = playerRepository.saveAndFlush(player("Target " + status));
            User requester = userRepository.saveAndFlush(user(status.name().toLowerCase() + "@example.com",
                    Role.USER, currentPlayer));
            recordSubmissionRepository.saveAndFlush(record(currentPlayer, demon, status));

            assertFalse(userService.canRequestPlayerClaim(requester));
            assertThrows(BadRequestException.class,
                    () -> playerClaimService.requestClaim(requester.getId(), targetPlayer.getId()));
        }
    }

    private Player player(String name) {
        Player player = new Player();
        player.setName(name);
        player.setPoints(0.0);
        return player;
    }

    private User user(String email, Role role, Player player) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setProvider(Provider.LOCAL);
        user.setEnabled(true);
        user.setPlayer(player);
        return user;
    }

    private Demon recordDemon() {
        Demon demon = new Demon();
        demon.setLevelTitle("Integration Demon");
        demon.setLevelId(123L);
        demon.setCreatorName("Creator");
        demon.setCreatorId(456L);
        demon.setDescription("A test demon");
        demon.setLevelPassword("password");
        demon.setMusicName("Track");
        demon.setMusicId(789L);
        demon.setMusicCreatorName("Artist");
        demon.setMusicUrl("https://example.com/music");
        demon.setRequirement(100);
        demon.setPosition(1);
        demon.setPoints(323.0);
        demon.setDifficulty(DemonDifficulty.EXTREME);
        return demonRepository.saveAndFlush(demon);
    }

    private RecordSubmission record(Player player, Demon demon, RecordSubmissionStatus status) {
        RecordSubmission submission = new RecordSubmission();
        submission.setProgress(100);
        submission.setYoutubeUrl("https://youtube.com/watch?v=integration");
        submission.setRawFootageUrl("https://example.com/raw");
        submission.setDescription("Integration test record");
        submission.setStatus(status);
        submission.setHolder(player);
        submission.setDemon(demon);
        return submission;
    }
}
