package com.bms.library.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long getUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated JWT is required"
            );
        }

        Number userId = jwt.getClaim("userId");

        if (userId == null) {
            throw new IllegalStateException(
                    "JWT does not contain userId claim"
            );
        }

        return userId.longValue();
    }

    public static String getUsername(
            Authentication authentication
    ) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated JWT is required"
            );
        }

        return jwt.getSubject();
    }

    public static String getRole(
            Authentication authentication
    ) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated JWT is required"
            );
        }

        return jwt.getClaimAsString("role");
    }
}
