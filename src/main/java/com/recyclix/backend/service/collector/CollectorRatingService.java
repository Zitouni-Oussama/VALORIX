package com.recyclix.backend.service.collector;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.CollectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CollectorRatingService {

    private final CollectorRepository collectorRepository;
    private final CollectionRepository collectionRepository;

    @Transactional
    public void updateCollectorAverageRating(Long collectorId) {
        // 1. Récupérer toutes les notes des collections de ce collecteur
        BigDecimal avgRating = collectionRepository.findAllByCollectorId(collectorId)
                .stream()
                .filter(c -> c.getRating() != null)
                .map(Collection::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ratedCount = collectionRepository.findAllByCollectorId(collectorId)
                .stream()
                .filter(c -> c.getRating() != null)
                .count();

        if (ratedCount > 0) {
            avgRating = avgRating.divide(BigDecimal.valueOf(ratedCount), 2, RoundingMode.HALF_UP);
        } else {
            avgRating = BigDecimal.ZERO;
        }

        // 2. Mettre à jour le collecteur
        collectorRepository.updateRating(collectorId, avgRating);
    }
}