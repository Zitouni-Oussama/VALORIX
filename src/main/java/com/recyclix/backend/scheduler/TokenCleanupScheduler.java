package com.recyclix.backend.scheduler;

import com.recyclix.backend.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final PasswordResetTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 * * * *")  // Toutes les heures
    @Transactional
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteAllExpiredTokens(now);
        log.info("Tokens expirés supprimés");
    }
}