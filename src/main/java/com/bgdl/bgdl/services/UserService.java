package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.response.AdminUserResponse;
import com.bgdl.bgdl.models.entity.DiscordProfile;
import com.bgdl.bgdl.models.dto.auth.OAuth2UserInfoDTO;
import com.bgdl.bgdl.models.request.ProfileUpdateRequest;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);

    User findByEmail(String email);

    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getByIdAdmin(UUID id);

    AdminUserResponse updateUser(UUID id, AdminUserResponse userDTO, PublicUserResponse currentUser);

    PublicUserResponse updateProfile(UUID id, ProfileUpdateRequest request, PublicUserResponse currentUser);

    void deleteUserById(UUID id, PublicUserResponse currentUser);

    User processOAuthUser(OAuth2UserInfoDTO oAuth2User);

    User findById(UUID id);

    void enableUser(User user);

    PublicUserResponse toPublicUserResponse(User user);

    PublicUserResponse linkDiscordAccount(UUID userId, DiscordProfile discordProfile);

    PublicUserResponse unlinkDiscordAccount(UUID userId);
}
