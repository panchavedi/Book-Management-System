package com.bms.userservice.service;

import com.bms.userservice.dto.*;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long TOKEN_EXPIRY_SECONDS = 3600;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(
                request.getUsername())) {

            throw new IllegalArgumentException(
                    "Username already exists");
        }

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role("USER")
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();

        return RegisterResponse.builder()
                .message("Registration successful")
                .user(userDto)
                .build();
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(TOKEN_EXPIRY_SECONDS)
                .user(userDto)
                .build();
    }

    public TokenValidationResponse validateToken(String token) {

        try {
            Jwt jwt = jwtDecoder.decode(token);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(jwt.getSubject())
                    .userId(jwt.getClaim("userId"))
                    .role(jwt.getClaim("role"))
                    .message("Token is valid")
                    .build();

        } catch (JwtException e) {

            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build();
        }
    }
}