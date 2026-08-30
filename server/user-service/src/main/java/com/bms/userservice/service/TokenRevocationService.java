package com.bms.userservice.service;

import com.bms.userservice.entity.RevokedToken;
import com.bms.userservice.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository repository;

    @Transactional
    public void revoke(String token, Instant expiresAt) {
        String hash = hash(token);
        if (!repository.existsByTokenHash(hash)) {
            repository.save(RevokedToken.builder()
                    .tokenHash(hash)
                    .expiresAt(expiresAt)
                    .revokedAt(Instant.now())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        return repository.existsByTokenHash(hash(token));
    }

    @Transactional
    public long removeExpiredTokens() {
        return repository.deleteByExpiresAtBefore(Instant.now());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
