package com.bgdl.bgdl.services.auth;

import com.bgdl.bgdl.models.dto.auth.AuthenticationSession;
import com.bgdl.bgdl.models.request.auth.AuthenticationRequest;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;

public interface AuthenticationService {
    AuthenticationSession register(RegisterRequest request);

    AuthenticationSession authenticate(AuthenticationRequest request);

    AuthenticationSession refreshToken(String refreshToken);

    AuthenticationResponse me(String jwtToken);

    void resetPassword(String token, String newPassword);

    void confirmRegistration(String verificationToken);

    User forgotPassword(String email);
}
