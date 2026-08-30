package com.bms.userservice.repository;

import com.bms.userservice.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByTokenHash(String tokenHash);
    long deleteByExpiresAtBefore(Instant cutoff);
}
