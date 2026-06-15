package com.recyclix.backend.service.admin;

import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final CollectorRepository collectorRepository;
    private final FactoryUserRepository factoryUserRepository;

    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionRepository collectionRepository;

    private final FactoryInvoiceRepository factoryInvoiceRepository;
    private final WalletRepository walletRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final MaterialRepository materialRepository;

    private static final BigDecimal POINT_VALUE_IN_DA = new BigDecimal("0.5");

    public AdminDashboardResponse getDashboard() {

        Long totalAccounts = accountRepository.count();
        Long totalClients = clientRepository.count();
        Long totalCollectors = collectorRepository.count();
        Long totalFactoryUsers = factoryUserRepository.count();

        Long totalCollectionRequests = collectionRequestRepository.count();
        Long pendingRequests = collectionRequestRepository.countByStatus(CollectionRequest.Status.PENDING);
        Long acceptedRequests = collectionRequestRepository.countByStatus(CollectionRequest.Status.ACCEPTED);
        Long completedRequests = collectionRequestRepository.countByStatus(CollectionRequest.Status.COLLECTED);
        Long cancelledRequests = collectionRequestRepository.countByStatus(CollectionRequest.Status.CANCELLED);

        Long totalCollections = collectionRepository.count();

        BigDecimal totalCollectedKg = safe(
                collectionRepository.findAll()
                        .stream()
                        .map(c -> c.getRealQuantity() != null ? c.getRealQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        BigDecimal totalCollectionAmount = safe(
                collectionRepository.findAll()
                        .stream()
                        .map(c -> c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        BigDecimal collectedRevenue = safe(
                factoryInvoiceRepository.sumAmountByStatuses(
                        java.util.List.of(FactoryInvoice.InvoiceStatus.PAID)
                )
        );

        BigDecimal expectedRevenue = safe(
                factoryInvoiceRepository.sumAmountByStatuses(
                        java.util.List.of(
                                FactoryInvoice.InvoiceStatus.PENDING,
                                FactoryInvoice.InvoiceStatus.OVERDUE
                        )
                )
        );

        Long overdueInvoices = factoryInvoiceRepository.countByStatus(FactoryInvoice.InvoiceStatus.OVERDUE);

        Long totalCitizenPoints = walletRepository.sumClientPoints();
        if (totalCitizenPoints == null) {
            totalCitizenPoints = 0L;
        }

        BigDecimal citizenLiabilities = BigDecimal
                .valueOf(totalCitizenPoints)
                .multiply(POINT_VALUE_IN_DA);

        Long totalMaterials = materialRepository.count();

        Long totalSupportTickets = supportTicketRepository.count();
        Long openSupportTickets = supportTicketRepository.countByStatus(SupportTicket.Status.OPEN);

        Long totalAlerts = overdueInvoices + openSupportTickets + pendingRequests;

        return AdminDashboardResponse.builder()
                .totalAccounts(totalAccounts)
                .totalClients(totalClients)
                .totalCollectors(totalCollectors)
                .totalFactoryUsers(totalFactoryUsers)

                .totalCollectionRequests(totalCollectionRequests)
                .pendingRequests(pendingRequests)
                .acceptedRequests(acceptedRequests)
                .completedRequests(completedRequests)
                .cancelledRequests(cancelledRequests)

                .totalCollections(totalCollections)
                .totalCollectedKg(totalCollectedKg)
                .totalCollectionAmount(totalCollectionAmount)

                .collectedRevenue(collectedRevenue)
                .expectedRevenue(expectedRevenue)
                .overdueInvoices(overdueInvoices)

                .totalCitizenPoints(totalCitizenPoints)
                .citizenLiabilities(citizenLiabilities)

                .totalMaterials(totalMaterials)

                .totalSupportTickets(totalSupportTickets)
                .openSupportTickets(openSupportTickets)

                .totalAlerts(totalAlerts)
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminDashboardResponse {

        private Long totalAccounts;
        private Long totalClients;
        private Long totalCollectors;
        private Long totalFactoryUsers;

        private Long totalCollectionRequests;
        private Long pendingRequests;
        private Long acceptedRequests;
        private Long completedRequests;
        private Long cancelledRequests;

        private Long totalCollections;
        private BigDecimal totalCollectedKg;
        private BigDecimal totalCollectionAmount;

        private BigDecimal collectedRevenue;
        private BigDecimal expectedRevenue;
        private Long overdueInvoices;

        private Long totalCitizenPoints;
        private BigDecimal citizenLiabilities;

        private Long totalMaterials;

        private Long totalSupportTickets;
        private Long openSupportTickets;

        private Long totalAlerts;
    }
}