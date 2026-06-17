package com.recyclix.backend.event;

import com.recyclix.backend.service.challenge.ChallengeProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollectionEventListener {

    private final ChallengeProgressService challengeProgressService;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)  
    public void handleCollectionCompleted(CollectionCompletedEvent event) {
        log.info("Événement de collecte reçu: {}", event.getCollection().getId());
        challengeProgressService.updateProgressAfterCollection(event.getCollection());
    }
}