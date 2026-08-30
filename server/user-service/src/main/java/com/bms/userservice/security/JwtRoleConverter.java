package com.bms.userservice.security;

import com.bms.userservice.service.TokenRevocationService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final TokenRevocationService tokenRevocationService;

    public JwtRoleConverter(TokenRevocationService tokenRevocationService) {
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (tokenRevocationService.isRevoked(jwt.getTokenValue())) {
            throw new BadCredentialsException("Token has been revoked");
        }

        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            throw new BadCredentialsException("Token does not contain a valid role");
        }

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
