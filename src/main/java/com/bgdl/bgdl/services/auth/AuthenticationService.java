package com.bgdl.bgdl.services.auth;

import com.bgdl.bgdl.models.request.auth.AuthenticationRequest;
import com.bgdl.bgdl.models.response.auth.AuthenticationResponse;
import com.bgdl.bgdl.models.request.auth.RegisterRequest;
import com.bgdl.bgdl.models.entity.User;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(String refreshToken) throws IOException;

    AuthenticationResponse me(
            String jwtToken
    );

    void resetPassword(String token, String newPassword);

    void confirmRegistration(String verificationToken);

    User forgotPassword(String email);
}
