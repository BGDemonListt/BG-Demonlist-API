package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.response.AdminUserResponse;
import com.bgdl.bgdl.models.dto.OAuth2UserInfoDTO;
import com.bgdl.bgdl.models.response.PublicUserResponse;
import com.bgdl.bgdl.models.request.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);

    User findByEmail(String email);

    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getByIdAdmin(UUID id);

    AdminUserResponse updateUser(UUID id, AdminUserResponse userDTO, PublicUserResponse currentUser);

    void deleteUserById(UUID id, PublicUserResponse currentUser);

    User processOAuthUser(OAuth2UserInfoDTO oAuth2User);

    User findById(UUID id);

    void enableUser(User user);
}
