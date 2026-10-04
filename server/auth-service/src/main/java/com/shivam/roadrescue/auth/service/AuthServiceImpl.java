package com.shivam.roadrescue.auth.service;

import com.shivam.roadrescue.auth.dto.request.LoginRequest;
import com.shivam.roadrescue.auth.dto.request.RefreshTokenRequest;
import com.shivam.roadrescue.auth.dto.request.RegisterRequest;
import com.shivam.roadrescue.auth.dto.response.AuthResponse;
import com.shivam.roadrescue.auth.dto.response.UserResponse;
import com.shivam.roadrescue.auth.entity.User;
import com.shivam.roadrescue.auth.mapper.UserMapper;
import com.shivam.roadrescue.auth.repository.UserRepository;
import com.shivam.roadrescue.auth.validation.PasswordValidator;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import com.shivam.roadrescue.shared.exception.ResourceNotFoundException;
import com.shivam.roadrescue.shared.exception.UnauthorizedAccessException;
import com.shivam.roadrescue.shared.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimiterService rateLimiterService;
    private final PasswordValidator passwordValidator;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService,
            RateLimiterService rateLimiterService,
            PasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.rateLimiterService = rateLimiterService;
        this.passwordValidator = passwordValidator;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User already exists with email: " + normalizedEmail);
        }

        passwordValidator.validate(request.getPassword());

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null)
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered successfully: id={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        UserResponse userResponse = userMapper.toResponse(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, String clientIdentifier) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        String rateLimitKey = clientIdentifier + ":" + normalizedEmail;

        rateLimiterService.checkAndIncrement(rateLimitKey);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedAccessException("Account is disabled. Please contact administrator.");
        }

        rateLimiterService.reset(rateLimitKey);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        UserResponse userResponse = userMapper.toResponse(user);
        log.info("User logged in successfully: id={}, email={}", user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedAccessException("Invalid or expired refresh token");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedAccessException("Token has been revoked");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.isEnabled()) {
            throw new UnauthorizedAccessException("Account is disabled");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userResponse)
                .build();
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            tokenBlacklistService.blacklistToken(token, 604800000L);
            log.info("User logged out and token invalidated");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
