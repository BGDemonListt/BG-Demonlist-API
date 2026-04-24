package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.BulgarianRegion;
import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.Role;
import com.bgdl.bgdl.exceptions.common.AccessDeniedException;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.exceptions.user.DiscordAccountAlreadyLinkedException;
import com.bgdl.bgdl.exceptions.user.UserCreateException;
import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.exceptions.user.UserValidationException;
import com.bgdl.bgdl.models.entity.Player;
import com.bgdl.bgdl.models.request.ProfileUpdateRequest;
import com.bgdl.bgdl.models.response.AdminUserResponse;
import com.bgdl.bgdl.models.dto.auth.OAuth2UserInfoDTO;
import com.bgdl.bgdl.models.entity.DiscordProfile;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;
import com.bgdl.bgdl.repositories.UserRepository;
import com.bgdl.bgdl.services.UserService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Creates a new user based on the provided registration request.
     *
     * @param request The registration request containing user details.
     * @return The created user.
     * @throws UserCreateException             If there is an issue creating the user.
     * @throws DataIntegrityViolationException If there is a data integrity violation while creating the user.
     * @throws ConstraintViolationException    If there is a constraint violation while creating the user.
     */
    @Override
    public User createUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserCreateException(true);
        }

        try {
            User user = buildUser(request);
            user.setRole(Role.USER);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setEnabled(false);

            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserCreateException(true);
        } catch (ConstraintViolationException exception) {
            throw new UserValidationException(exception.getConstraintViolations());
        }
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(UUID id, AdminUserResponse userDTO, PublicUserResponse currentUser) {
        User userToUpdate = findById(id);
        DiscordProfile currentDiscordProfile = userToUpdate.getDiscord();

        if (!(userToUpdate.getId().equals(currentUser.getId())) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException();
        }

        if (currentUser.getRole().equals(Role.ADMIN)) {
            // It is not null it is "" so don't change it
            if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
                userDTO.setPassword(userToUpdate.getPassword());
            } else {
                userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            modelMapper.map(userDTO, userToUpdate);
            userToUpdate.setDiscord(currentDiscordProfile);
            syncPlayerName(userToUpdate, userDTO.getName());
        } else {
            updateProfileFields(userToUpdate, userDTO.getName(), null);
        }

        userToUpdate.setId(id);

        User updatedUser = userRepository.save(userToUpdate);
        return toAdminUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public PublicUserResponse updateProfile(UUID id, ProfileUpdateRequest request, PublicUserResponse currentUser) {
        User userToUpdate = findById(id);

        if (!(userToUpdate.getId().equals(currentUser.getId())) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException();
        }

        updateProfileFields(userToUpdate, request.getName(), request.getRegion());

        return toPublicUserResponse(userRepository.save(userToUpdate));
    }


    @Override
    public void deleteUserById(UUID id, PublicUserResponse currentUser) {
        User user = findById(id);

        if (user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        if (user.getDeletedAt() == null) {
            user.setDeletedAt(LocalDateTime.now());
        } else {
            user.setDeletedAt(null);
        }

        userRepository.save(user);
    }

    /**
     * Processes the OAuth user obtained from the OAuth2 provider.
     * If the user does not exist in the database, a new user is created based on the OAuth user details.
     *
     * @param oAuth2User The OAuth2 user obtained from the OAuth provider.
     * @return The processed user.
     */
    @Override
    public User processOAuthUser(OAuth2UserInfoDTO oAuth2User) {
        User user = userRepository.findByEmail(oAuth2User.getEmail()).orElse(null);

        if (user == null) {
            RegisterRequest registerRequest = new RegisterRequest();

            registerRequest.setEmail(oAuth2User.getEmail());
            registerRequest.setProvider(oAuth2User.getProvider());

            if (oAuth2User.getProvider().equals(Provider.GOOGLE)) {
                registerRequest.setName(oAuth2User.getGiven_name());
            }

            user = userRepository.save(buildUser(registerRequest));
        }

        return user;
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public AdminUserResponse getByIdAdmin(UUID id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return toAdminUserResponse(user);
    }

    private User buildUser(RegisterRequest request) {
        User.UserBuilder userBuilder = User
                .builder()
                .name(request.getName())
                .email(request.getEmail())
                .provider(request.getProvider())
                .role(Role.USER);

        if (request.getPassword() != null) {
            userBuilder.password(passwordEncoder.encode(request.getPassword()));
        }

        return userBuilder.build();
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        AdminUserResponse response = modelMapper.map(user, AdminUserResponse.class);
        response.setPlayerId(user.getPlayer() != null ? user.getPlayer().getId() : null);
        return response;
    }

    private void updateProfileFields(User user, String name, BulgarianRegion region) {
        Player player = user.getPlayer();

        if (player == null) {
            throw new BadRequestException("Профилът няма свързан играч!");
        }

        if (name != null) {
            String trimmedName = name.trim();
            if (trimmedName.length() < 2) {
                throw new BadRequestException("Името трябва да е поне 2 символа!");
            }

            user.setName(trimmedName);
            player.setName(trimmedName);
        }

        if (region != null) {
            player.setRegion(region);
        }
    }

    private void syncPlayerName(User user, String name) {
        if (user.getPlayer() != null && name != null) {
            user.getPlayer().setName(name.trim());
        }
    }

    @Override
    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public PublicUserResponse toPublicUserResponse(User user) {
        PublicUserResponse response = modelMapper.map(user, PublicUserResponse.class);
        response.setPlayerId(user.getPlayer() != null ? user.getPlayer().getId() : null);
        return response;
    }

    @Override
    @Transactional
    public PublicUserResponse linkDiscordAccount(UUID userId, DiscordProfile discordProfile) {
        User user = findById(userId);

        userRepository.findByDiscord_Id(discordProfile.getId())
                .filter(existingUser -> !existingUser.getId().equals(userId))
                .ifPresent(existingUser -> {
                    throw new DiscordAccountAlreadyLinkedException();
                });

        user.setDiscord(discordProfile);
        return toPublicUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public PublicUserResponse unlinkDiscordAccount(UUID userId) {
        User user = findById(userId);
        user.setDiscord(null);
        return toPublicUserResponse(userRepository.save(user));
    }
}
