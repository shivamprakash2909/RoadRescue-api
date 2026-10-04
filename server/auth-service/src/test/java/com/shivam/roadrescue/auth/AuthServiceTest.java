package com.shivam.roadrescue.auth;

import com.shivam.roadrescue.auth.dto.request.LoginRequest;
import com.shivam.roadrescue.auth.dto.request.RefreshTokenRequest;
import com.shivam.roadrescue.auth.dto.request.RegisterRequest;
import com.shivam.roadrescue.auth.dto.response.AuthResponse;
import com.shivam.roadrescue.auth.entity.User;
import com.shivam.roadrescue.auth.mapper.UserMapper;
import com.shivam.roadrescue.auth.repository.UserRepository;
import com.shivam.roadrescue.auth.service.AuthServiceImpl;
import com.shivam.roadrescue.auth.service.RateLimiterService;
import com.shivam.roadrescue.auth.service.TokenBlacklistService;
import com.shivam.roadrescue.auth.validation.PasswordValidator;
import com.shivam.roadrescue.shared.enums.Role;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import com.shivam.roadrescue.shared.exception.UnauthorizedAccessException;
import com.shivam.roadrescue.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RateLimiterService rateLimiterService;

    private UserMapper userMapper;
    private PasswordValidator passwordValidator;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        passwordValidator = new PasswordValidator();
        authService = new AuthServiceImpl(
                userRepository,
                userMapper,
                passwordEncoder,
                jwtTokenProvider,
                tokenBlacklistService,
                rateLimiterService,
                passwordValidator
        );
    }

    @Test
    @DisplayName("Should successfully register a new customer")
    void register_Customer_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("john@example.com")
                .password("Password123")
                .fullName("John Doe")
                .phone("+1234567890")
                .role(Role.CUSTOMER)
                .build();

        UUID generatedId = UUID.randomUUID();
        User savedUser = User.builder()
                .id(generatedId)
                .email("john@example.com")
                .fullName("John Doe")
                .passwordHash("encodedPassword")
                .phone("+1234567890")
                .role(Role.CUSTOMER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(eq(generatedId), eq("john@example.com"), eq(Role.CUSTOMER)))
                .thenReturn("mockAccessToken");
        when(jwtTokenProvider.generateRefreshToken(eq(generatedId)))
                .thenReturn("mockRefreshToken");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("Should successfully register a new mechanic provider")
    void register_Mechanic_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("mechanic@example.com")
                .password("SecurePass99")
                .fullName("Mike Mechanic")
                .role(Role.MECHANIC)
                .build();

        UUID generatedId = UUID.randomUUID();
        User savedUser = User.builder()
                .id(generatedId)
                .email("mechanic@example.com")
                .fullName("Mike Mechanic")
                .passwordHash("encodedPassword")
                .role(Role.MECHANIC)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.existsByEmail("mechanic@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass99")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(eq(generatedId), eq("mechanic@example.com"), eq(Role.MECHANIC)))
                .thenReturn("mechAccessToken");
        when(jwtTokenProvider.generateRefreshToken(eq(generatedId)))
                .thenReturn("mechRefreshToken");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo(Role.MECHANIC);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException on already registered email")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("Password123")
                .fullName("Existing User")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User already exists with email: existing@example.com");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on weak password")
    void register_WeakPassword_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("weak")
                .fullName("Weak Password User")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("Password123")
                .build();

        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("john@example.com")
                .fullName("John Doe")
                .passwordHash("hashedSecret")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password123", "hashedSecret")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(userId, "john@example.com", Role.CUSTOMER)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        verify(rateLimiterService).reset("127.0.0.1:john@example.com");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid password")
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .passwordHash("hashedSecret")
                .enabled(true)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPassword", "hashedSecret")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException on disabled account")
    void login_DisabledAccount_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .email("blocked@example.com")
                .password("Password123")
                .build();

        User disabledUser = User.builder()
                .id(UUID.randomUUID())
                .email("blocked@example.com")
                .passwordHash("hashedSecret")
                .enabled(false)
                .build();

        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches("Password123", "hashedSecret")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Account is disabled");
    }

    @Test
    @DisplayName("Should successfully refresh access token using valid refresh token")
    void refreshToken_Success() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .fullName("John Doe")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("valid-refresh-token")).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(userId, "john@example.com", Role.CUSTOMER)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("Should blacklist token on logout")
    void logout_BlacklistsToken() {
        String token = "Bearer sample-jwt-token";

        authService.logout(token);

        verify(tokenBlacklistService).blacklistToken(token, 604800000L);
    }
}
