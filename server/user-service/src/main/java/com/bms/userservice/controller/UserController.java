package com.bms.userservice.controller;

import com.bms.userservice.dto.AdminCreateUserRequest;
import com.bms.userservice.dto.RegisterResponse;
import com.bms.userservice.dto.UserDto;
import com.bms.userservice.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import com.bms.userservice.service.AuthService;
import com.bms.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<RegisterResponse> createByAdmin(
            @Valid @RequestBody AdminCreateUserRequest request,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createByAdmin(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        if (!isAdmin(authentication) && !id.equals(getAuthenticatedUserId(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            Long authenticatedUserId = getAuthenticatedUserId(authentication);
            if (!id.equals(authenticatedUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    private void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException("Administrator access required");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Object userId = jwtAuthenticationToken.getToken().getClaim("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
            if (userId != null) {
                try {
                    return Long.valueOf(userId.toString());
                } catch (NumberFormatException ignored) {
                    // Fall through to forbidden response below.
                }
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Unable to identify authenticated user");
    }
}
