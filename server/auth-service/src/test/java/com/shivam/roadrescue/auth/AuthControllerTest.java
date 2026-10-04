package com.shivam.roadrescue.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.roadrescue.auth.controller.AuthController;
import com.shivam.roadrescue.auth.dto.request.LoginRequest;
import com.shivam.roadrescue.auth.dto.request.RefreshTokenRequest;
import com.shivam.roadrescue.auth.dto.request.RegisterRequest;
import com.shivam.roadrescue.auth.dto.response.AuthResponse;
import com.shivam.roadrescue.auth.dto.response.UserResponse;
import com.shivam.roadrescue.auth.exception.GlobalExceptionHandler;
import com.shivam.roadrescue.auth.service.AuthService;
import com.shivam.roadrescue.shared.enums.Role;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - returns 201 Created on valid request")
    void register_ValidRequest_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .fullName("Test User")
                .phone("+1234567890")
                .role(Role.CUSTOMER)
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("test@example.com")
                        .fullName("Test User")
                        .role(Role.CUSTOMER)
                        .enabled(true)
                        .createdAt(Instant.now())
                        .build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - returns 400 Bad Request on invalid email")
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email-format")
                .password("Password123")
                .fullName("Test User")
                .role(Role.CUSTOMER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - returns 409 Conflict on duplicate email")
    void register_DuplicateEmail_Returns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("Password123")
                .fullName("Test User")
                .role(Role.CUSTOMER)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User already exists with email: existing@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User already exists with email: existing@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - returns 200 OK on valid credentials")
    void login_ValidCredentials_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("test@example.com")
                        .fullName("Test User")
                        .role(Role.CUSTOMER)
                        .enabled(true)
                        .createdAt(Instant.now())
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - returns 401 Unauthorized on invalid credentials")
    void login_BadCredentials_Returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class), anyString()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - returns 200 OK on valid refresh token")
    void refresh_ValidToken_Returns200() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("valid-refresh-token")
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("test@example.com")
                        .fullName("Test User")
                        .role(Role.CUSTOMER)
                        .enabled(true)
                        .build())
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - returns 200 OK")
    void logout_Returns200() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer sample-token"))
                .andExpect(status().isOk());
    }
}
