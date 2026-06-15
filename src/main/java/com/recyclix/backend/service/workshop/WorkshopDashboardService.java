package com.recyclix.backend.service.workshop;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.FactoryUserMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopDashboardService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;

    private final FactoryDeliveryRepository factoryDeliveryRepository;
    private final FactoryValidationRepository factoryValidationRepository;
    private final MachineRepository machineRepository;
    private final MachineIncidentRepository machineIncidentRepository;
    private final CollectionRepository collectionRepository;
    private final MaterialRepository materialRepository;

    private final AccountMapper accountMapper;
    private final FactoryUserMapper factoryUserMapper;

    @Transactional(readOnly = true)
    public WorkshopDashboardResponse getDashboard() {
        Account account = getAuthenticatedFactoryUserAccount();
        FactoryUser factoryUser = getFactoryUserFromAccount(account);

        List<FactoryDelivery> deliveries = factoryDeliveryRepository.findAll();
        List<FactoryValidation> validations = factoryValidationRepository.findAll();
        List<Machine> machines = machineRepository.findAll();
        List<MachineIncident> incidents = machineIncidentRepository.findAll();
        List<Collection> collections = collectionRepository.findAll();
        List<Material> materials = materialRepository.findAll();

        LocalDate today = LocalDate.now();

        long deliveriesToday = deliveries.stream()
                .filter(d -> isSameDay(d.getDeliveryDate(), today) || isSameDay(d.getCreatedAt(), today))
                .count();

        long pendingValidations = deliveries.stream()
                .filter(d -> d.getValidation() == null)
                .count();

        long validatedDeliveries = validations.stream()
                .filter(v -> !StringUtils.hasText(v.getRejectionReason()))
                .count();

        long rejectedDeliveries = validations.stream()
                .filter(v -> StringUtils.hasText(v.getRejectionReason()))
                .count();

        long activeMachines = machines.stream()
                .filter(this::isMachineActive)
                .count();

        long openIncidents = incidents.stream()
                .filter(i -> i.getResolvedAt() == null)
                .count();

        long totalCollections = collections.size();
        long totalMaterials = materials.size();

        return WorkshopDashboardResponse.builder()
                .account(accountMapper.toDto(account))
                .factoryUser(factoryUserMapper.toDto(factoryUser))
                .kpis(WorkshopKpis.builder()
                        .deliveriesToday(deliveriesToday)
                        .pendingValidations(pendingValidations)
                        .validatedDeliveries(validatedDeliveries)
                        .rejectedDeliveries(rejectedDeliveries)
                        .activeMachines(activeMachines)
                        .openIncidents(openIncidents)
                        .totalCollections(totalCollections)
                        .totalMaterials(totalMaterials)
                        .build())
                .alerts(buildAlerts(deliveriesToday, pendingValidations, openIncidents, rejectedDeliveries))
                .build();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Account getAuthenticatedFactoryUserAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.FACTORY_USER) {
            throw new UnauthorizedException("Accès réservé au chef d’atelier.");
        }

        return account;
    }

    private FactoryUser getFactoryUserFromAccount(Account account) {
        if (account.getFactoryUser() != null) {
            return account.getFactoryUser();
        }

        return factoryUserRepository.findAll().stream()
                .filter(fu -> fu.getAccount() != null && fu.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé usine introuvable."));
    }

    private boolean isSameDay(LocalDateTime dateTime, LocalDate day) {
        return dateTime != null && dateTime.toLocalDate().equals(day);
    }

    private boolean isMachineActive(Machine machine) {
        if (machine == null || machine.getStatus() == null) {
            return false;
        }

        String status = machine.getStatus().name().toUpperCase();

        return status.equals("ACTIVE")
                || status.equals("RUNNING")
                || status.equals("WORKING")
                || status.equals("EN_MARCHE");
    }

    private List<DashboardAlert> buildAlerts(
            long deliveriesToday,
            long pendingValidations,
            long openIncidents,
            long rejectedDeliveries
    ) {
        return List.of(
                DashboardAlert.builder()
                        .type("INFO")
                        .title("Livraisons du jour")
                        .message("Nombre de livraisons reçues aujourd’hui : " + deliveriesToday)
                        .build(),

                DashboardAlert.builder()
                        .type(pendingValidations > 0 ? "WARNING" : "SUCCESS")
                        .title("Validations en attente")
                        .message("Nombre de livraisons en attente de validation : " + pendingValidations)
                        .build(),

                DashboardAlert.builder()
                        .type(openIncidents > 0 ? "DANGER" : "SUCCESS")
                        .title("Incidents machines")
                        .message("Nombre d’incidents ouverts : " + openIncidents)
                        .build(),

                DashboardAlert.builder()
                        .type(rejectedDeliveries > 0 ? "WARNING" : "INFO")
                        .title("Livraisons rejetées")
                        .message("Nombre total de livraisons rejetées : " + rejectedDeliveries)
                        .build()
        );
    }

    // =========================================================
    // RESPONSE CLASSES
    // =========================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkshopDashboardResponse {
        private AccountResponseDTO account;
        private FactoryUserResponseDTO factoryUser;
        private WorkshopKpis kpis;
        private List<DashboardAlert> alerts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkshopKpis {
        private Long deliveriesToday;
        private Long pendingValidations;
        private Long validatedDeliveries;
        private Long rejectedDeliveries;
        private Long activeMachines;
        private Long openIncidents;
        private Long totalCollections;
        private Long totalMaterials;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardAlert {
        private String type;
        private String title;
        private String message;
    }
}