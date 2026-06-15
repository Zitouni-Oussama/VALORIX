package com.recyclix.backend.scheduler;

import com.recyclix.backend.service.challenge.ChallengeProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeStreakScheduler {

    private final ChallengeProgressService challengeProgressService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateDailyStreaks() {
        log.info("Démarrage de la mise à jour quotidienne des streaks de défis");
        try {
            challengeProgressService.updateDailyStreaks();
            log.info("Mise à jour des streaks terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des streaks", e);
        }
    }
}