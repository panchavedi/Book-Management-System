package com.bms.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenRevocationCleanup {

    private final TokenRevocationService tokenRevocationService;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRevocations() {
        tokenRevocationService.removeExpiredTokens();
    }
}
