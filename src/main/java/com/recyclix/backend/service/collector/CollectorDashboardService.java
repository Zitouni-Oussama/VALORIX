package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collection.CollectionSummaryDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.dto.wallet.WalletResponseDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.CollectorLocationHistoryMapper;
import com.recyclix.backend.mapper.CollectorMapper;
import com.recyclix.backend.mapper.CollectionMapper;
import com.recyclix.backend.mapper.PaymentMapper;
import com.recyclix.backend.mapper.TruckMapper;
import com.recyclix.backend.mapper.WalletMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.CollectorLocationHistory;
import com.recyclix.backend.model.Payment;
import com.recyclix.backend.model.Truck;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.CollectionRequestRepository;
import com.recyclix.backend.repository.PaymentRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorDashboardService {

    private final AccountRepository accountRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionRepository collectionRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    private final CollectorMapper collectorMapper;
    private final TruckMapper truckMapper;
    private final WalletMapper walletMapper;
    private final CollectionMapper collectionMapper;
    private final PaymentMapper paymentMapper;
    private final CollectorLocationHistoryMapper collectorLocationHistoryMapper;

    @Transactional(readOnly = true)
    public CollectorDashboardResponse getDashboard() {
        Account account = getAuthenticatedCollectorAccount();
        Collector collector = getAuthenticatedCollector(account);

        CollectorResponseDTO collectorDto = collectorMapper.toDto(collector);

        TruckResponseDTO truckDto = null;
        Truck truck = collector.getTruck();
        if (truck != null) {
            truckDto = truckMapper.toResponseDTO(truck);
        }

        WalletResponseDTO walletDto = null;
        Wallet wallet = walletRepository.findByAccountId(account.getId()).orElse(null);
        if (wallet != null) {
            walletDto = walletMapper.toResponseDTO(wallet);
            Long txCount = account.getTransactions() != null ? (long) account.getTransactions().size() : 0L;
            walletDto.setTransactions(txCount.intValue());
        }

        CollectorLocationHistoryResponseDTO locationDto = null;
        CollectorLocationHistory location = collector.getCollectorLocationHistory();
        if (location != null) {
            locationDto = collectorLocationHistoryMapper.toDto(location);
        }

        long availableRequestsCount = collectionRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.PENDING)
                .filter(r -> r.getCollection() == null)
                .count();

        long activeRequestsCount = collectionRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.ACCEPTED)
                .filter(r -> r.getCollection() == null)
                .count();

        List<Collection> myCollections = collectionRepository.findAll().stream()
                .filter(c -> c.getCollector() != null && c.getCollector().getId().equals(collector.getId()))
                .sorted(Comparator.comparing(Collection::getCollectedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        long completedCollectionsCount = myCollections.size();

        List<CollectionSummaryDTO> recentCollections = myCollections.stream()
                .limit(5)
                .map(collectionMapper::toSummaryDto)
                .toList();

        List<Payment> myPayments = paymentRepository.findAllByAccountIdOrderByPaymentDateDesc(account.getId());
        List<PaymentResponseDTO> recentPayments = myPayments.stream()
                .limit(5)
                .map(paymentMapper::toResponseDTO)
                .toList();

        BigDecimal totalPaymentsReceived = myPayments.stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CollectorDashboardResponse.builder()
                .collector(collectorDto)
                .truck(truckDto)
                .wallet(walletDto)
                .currentLocation(locationDto)
                .availableRequestsCount(availableRequestsCount)
                .activeRequestsCount(activeRequestsCount)
                .completedCollectionsCount(completedCollectionsCount)
                .totalPaymentsReceived(totalPaymentsReceived)
                .recentCollections(recentCollections)
                .recentPayments(recentPayments)
                .build();
    }

    private Account getAuthenticatedCollectorAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new UnauthorizedException("Accès réservé au collecteur.");
        }

        return account;
    }

    private Collector getAuthenticatedCollector(Account account) {
        if (account.getCollector() == null) {
            throw new ResourceNotFoundException("Profil collecteur introuvable.");
        }
        return account.getCollector();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectorDashboardResponse {
        private CollectorResponseDTO collector;
        private TruckResponseDTO truck;
        private WalletResponseDTO wallet;
        private CollectorLocationHistoryResponseDTO currentLocation;

        private long availableRequestsCount;
        private long activeRequestsCount;
        private long completedCollectionsCount;

        private BigDecimal totalPaymentsReceived;

        private List<CollectionSummaryDTO> recentCollections;
        private List<PaymentResponseDTO> recentPayments;
    }
}