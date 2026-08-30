package com.bms.userservice.service;

import com.bms.userservice.dto.AdminCreateUserRequest;
import com.bms.userservice.dto.LoginRequest;
import com.bms.userservice.dto.LoginResponse;
import com.bms.userservice.dto.LogoutResponse;
import com.bms.userservice.dto.RegisterRequest;
import com.bms.userservice.dto.RegisterResponse;
import com.bms.userservice.dto.TokenValidationResponse;
import com.bms.userservice.dto.UserDto;
import com.bms.userservice.entity.User;
import com.bms.userservice.repository.UserRepository;
import com.bms.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long TOKEN_EXPIRY_SECONDS = 3600;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;
    private final TokenRevocationService tokenRevocationService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone().trim())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .fullName(trimToNull(request.getFullName()))
                .phone(trimToNull(request.getPhone()))
                .address(trimToNull(request.getAddress()))
                .build();

        User savedUser = userRepository.save(user);
        return RegisterResponse.builder()
                .message("Registration successful")
                .user(toDto(savedUser))
                .build();
    }

    @Transactional
    public RegisterResponse createByAdmin(AdminCreateUserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();
        String role = request.getRole().trim().toUpperCase();

        if (!java.util.Set.of("USER", "ADMIN", "AUTHOR", "LIBRARIAN").contains(role)) {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .fullName(trimToNull(request.getFullName()))
                .phone(trimToNull(phone))
                .address(trimToNull(request.getAddress()))
                .build();

        return RegisterResponse.builder()
                .message("User created successfully")
                .user(toDto(userRepository.save(user)))
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(TOKEN_EXPIRY_SECONDS)
                .user(toDto(user))
                .build();
    }

    @Transactional
    public LogoutResponse logout(String token) {
        Jwt jwt = decode(token);
        tokenRevocationService.revoke(token, jwt.getExpiresAt());
        return LogoutResponse.builder().message("Logout successful").build();
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            Jwt jwt = decode(token);
            if (tokenRevocationService.isRevoked(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token has been revoked")
                        .build();
            }

            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(jwt.getSubject())
                    .userId(jwt.getClaim("userId"))
                    .role(jwt.getClaim("role"))
                    .message("Token is valid")
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build();
        }
    }

    private Jwt decode(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        return jwtDecoder.decode(token);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
