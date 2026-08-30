package com.bms.userservice.controller;

import com.bms.userservice.dto.LoginRequest;
import com.bms.userservice.dto.LoginResponse;
import com.bms.userservice.dto.LogoutResponse;
import com.bms.userservice.dto.RegisterRequest;
import com.bms.userservice.dto.RegisterResponse;
import com.bms.userservice.dto.TokenValidationResponse;
import com.bms.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.logout(authorization.substring(7).trim()));
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestParam String token) {
        TokenValidationResponse response = authService.validateToken(token);
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
