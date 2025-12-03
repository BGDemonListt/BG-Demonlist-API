package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.request.AuthenticationRequest;
import com.bgdl.bgdl.models.response.AuthenticationResponse;
import com.bgdl.bgdl.models.request.RegisterRequest;
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
