package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collector.CollectorSummaryDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.CollectorMapper;
import com.recyclix.backend.mapper.TruckMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.Truck;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CollectorRepository;
import com.recyclix.backend.repository.TruckRepository;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCollectorVerificationService {

    private final CollectorRepository collectorRepository;
    private final TruckRepository truckRepository;
    private final AccountRepository accountRepository;
    private final CollectorMapper collectorMapper;
    private final TruckMapper truckMapper;

    @Transactional(readOnly = true)
    public Page<CollectorSummaryDTO> getAllCollectors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return collectorRepository.findAll(pageable)
                .map(collectorMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<CollectorSummaryDTO> getVerifiedCollectors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return collectorRepository.findAllByIsVerified(true, pageable)
                .map(collectorMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<CollectorSummaryDTO> getUnverifiedCollectors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return collectorRepository.findAllByIsVerified(false, pageable)
                .map(collectorMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public CollectorVerificationDetailResponse getCollectorVerificationDetails(Long collectorId) {
        Collector collector = getCollectorOrThrow(collectorId);

        TruckResponseDTO truckDto = null;
        Truck truck = collector.getTruck();

        if (truck != null) {
            truckDto = truckMapper.toResponseDTO(truck);
        }

        boolean hasTruck = truck != null;
        boolean hasTruckPhoto = hasTruck && hasText(truck.getTruckPhotoUrl());
        boolean hasGreyCard = hasTruck && hasText(truck.getGreyCardImageUrl());
        boolean hasDrivingLicense = hasTruck && hasText(truck.getDrivingLicenseImageUrl());

        boolean readyForVerification = hasTruck && hasGreyCard && hasDrivingLicense;
        String accountStatus = collector.getAccount().getStatus().name();

        return CollectorVerificationDetailResponse.builder()
                .collector(collectorMapper.toDto(collector))
                .truck(truckDto)
                .hasTruck(hasTruck)
                .hasTruckPhoto(hasTruckPhoto)
                .hasGreyCard(hasGreyCard)
                .hasDrivingLicense(hasDrivingLicense)
                .readyForVerification(readyForVerification)
                .accountStatus(accountStatus)
                .build();
    }

//    @Transactional
//    public CollectorResponseDTO verifyCollector(Long collectorId) {
//        Collector collector = getCollectorOrThrow(collectorId);
//
//        Truck truck = collector.getTruck();
//
//        if (truck == null) {
//            throw new BadRequestException("Le collecteur ne possède pas encore de camion.");
//        }
//
//        if (!hasText(truck.getGreyCardImageUrl())) {
//            throw new BadRequestException("La carte grise est obligatoire pour vérifier le collecteur.");
//        }
//
//        if (!hasText(truck.getDrivingLicenseImageUrl())) {
//            throw new BadRequestException("Le permis de conduire est obligatoire pour vérifier le collecteur.");
//        }
//
//        collector.setIsVerified(true);
//
//        return collectorMapper.toDto(collectorRepository.save(collector));
//    }

    @Transactional
    public CollectorResponseDTO unverifyCollector(Long collectorId) {
        Collector collector = getCollectorOrThrow(collectorId);

        collector.setIsVerified(false);

        return collectorMapper.toDto(collectorRepository.save(collector));
    }

    @Transactional(readOnly = true)
    public TruckResponseDTO getCollectorTruck(Long collectorId) {
        Collector collector = getCollectorOrThrow(collectorId);

        Truck truck = collector.getTruck();

        if (truck == null) {
            throw new ResourceNotFoundException("Aucun camion trouvé pour ce collecteur.");
        }

        return truckMapper.toResponseDTO(truck);
    }

    @Transactional(readOnly = true)
    public CollectorVerificationStatsResponse getVerificationStats() {
        Long totalCollectors = collectorRepository.count();
        Long verifiedCollectors = collectorRepository.countByIsVerified(true);
        Long unverifiedCollectors = collectorRepository.countByIsVerified(false);
        Long totalTrucks = truckRepository.count();

        return CollectorVerificationStatsResponse.builder()
                .totalCollectors(totalCollectors)
                .verifiedCollectors(verifiedCollectors)
                .unverifiedCollectors(unverifiedCollectors)
                .totalTrucks(totalTrucks)
                .build();
    }

    private Collector getCollectorOrThrow(Long collectorId) {
        return collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecteur introuvable avec id : " + collectorId));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Data
    public static class RejectCollectorRequest {
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectorVerificationDetailResponse {
        private CollectorResponseDTO collector;
        private TruckResponseDTO truck;

        private boolean hasTruck;
        private boolean hasTruckPhoto;
        private boolean hasGreyCard;
        private boolean hasDrivingLicense;
        private boolean readyForVerification;
        private String accountStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectorVerificationStatsResponse {
        private Long totalCollectors;
        private Long verifiedCollectors;
        private Long unverifiedCollectors;
        private Long totalTrucks;
    }


    @Transactional
    public CollectorResponseDTO rejectCollector(Long collectorId, RejectCollectorRequest request) {
        Collector collector = getCollectorOrThrow(collectorId);
        collector.setIsVerified(false);

        // Désactiver le compte associé
        Account account = collector.getAccount();
        if (account != null) {
            account.setStatus(Account.AccountStatus.INACTIVE);
            accountRepository.save(account);
        }

        return collectorMapper.toDto(collectorRepository.save(collector));
    }

    @Transactional
    public CollectorResponseDTO verifyCollector(Long collectorId) {
        Collector collector = getCollectorOrThrow(collectorId);
        Truck truck = collector.getTruck();
        if (truck == null) {
            throw new BadRequestException("Le collecteur ne possède pas encore de camion.");
        }
        if (!hasText(truck.getGreyCardImageUrl())) {
            throw new BadRequestException("La carte grise est obligatoire pour vérifier le collecteur.");
        }
        if (!hasText(truck.getDrivingLicenseImageUrl())) {
            throw new BadRequestException("Le permis de conduire est obligatoire pour vérifier le collecteur.");
        }
        collector.setIsVerified(true);

        // Réactiver le compte si inactif
        Account account = collector.getAccount();
        if (account != null && account.getStatus() != Account.AccountStatus.ACTIVE) {
            account.setStatus(Account.AccountStatus.ACTIVE);
            accountRepository.save(account);
        }

        return collectorMapper.toDto(collectorRepository.save(collector));
    }
}