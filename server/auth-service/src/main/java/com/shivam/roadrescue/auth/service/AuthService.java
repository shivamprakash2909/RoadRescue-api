package com.shivam.roadrescue.auth.service;

import com.shivam.roadrescue.auth.dto.request.LoginRequest;
import com.shivam.roadrescue.auth.dto.request.RefreshTokenRequest;
import com.shivam.roadrescue.auth.dto.request.RegisterRequest;
import com.shivam.roadrescue.auth.dto.response.AuthResponse;
import com.shivam.roadrescue.auth.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String clientIdentifier);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String token);

    UserResponse getCurrentUser(UUID userId);
}
