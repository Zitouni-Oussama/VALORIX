package com.recyclix.backend.controller.web;


import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountSummaryDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.service.admin.CancellationMonitoringService;
import com.recyclix.backend.dto.challenge.ChallengeRequestDTO;
import com.recyclix.backend.dto.challenge.ChallengeResponseDTO;
import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.ChallengeUpdateDTO;
import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collector.CollectorSummaryDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntryRequestDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntryResponseDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntryUpdateDTO;
import com.recyclix.backend.dto.machine.MachineRequestDTO;
import com.recyclix.backend.dto.machine.MachineResponseDTO;
import com.recyclix.backend.dto.machine.MachineSummaryDTO;
import com.recyclix.backend.dto.machine.MachineUpdateDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentResponseDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentSummaryDTO;
import com.recyclix.backend.dto.material.MaterialResponseDTO;
import com.recyclix.backend.dto.material.MaterialSummaryDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterRequestDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterResponseDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterSummaryDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterUpdateDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.mapper.ChallengeMapper;
import com.recyclix.backend.mapper.MachineMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CancellationLogRepository;
import com.recyclix.backend.repository.ChallengeRepository;
import com.recyclix.backend.repository.MachineRepository;
import com.recyclix.backend.service.admin.*;
import com.recyclix.backend.service.auth.PasswordService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.dto.faq_entry.*;
import com.recyclix.backend.dto.notification.*;
import com.recyclix.backend.dto.support_ticket.*;
import com.recyclix.backend.model.FaqEntry;
import com.recyclix.backend.model.Notification.NotificationType;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.service.admin.AdminNotificationAndLogService;
import com.recyclix.backend.service.admin.AdminSupportService;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
@RequiredArgsConstructor
public class AdminWebController {

    private final AdminDashboardService dashboardService;
    private final AdminCollectionSupervisionService supervisionService;
    private final AdminUserService userService;
    private final AdminPricingService pricingService;
    private final AdminNotificationAndLogService notificationService;
    private final AdminFactoryUserService adminFactoryUserService;
    private final PasswordService passwordService;
    private final AdminUserService adminUserService;
    private final AdminPricingService adminPricingService;
    private final AdminChallengeService adminChallengeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;
    private final AdminCollectorVerificationService adminCollectorVerificationService;
    private final AdminSupportService adminSupportService;
    private final AdminNotificationAndLogService adminNotificationAndLogService;
    private final AdminMachineService adminMachineService;
    private final MachineRepository machineRepository;
    private final AdminRecyclingCenterService adminRecyclingCenterService;
    private final AccountRepository accountRepository;

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminWebController.class);


    // ==================== DASHBOARD ====================
    // ==================== API DASHBOARD JSON ====================

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "admin/dashboard";
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public DashboardApiResponse getDashboardApi() {
        return DashboardApiResponse.builder()
                .stats(dashboardService.getDashboard())
                .supervisionStats(supervisionService.getSupervisionStats())
                .userStats(userService.getUserStats())
                .newUsersToday(userService.countNewUsersToday())
                .newUsersThisWeek(userService.countNewUsersThisWeek())
                .newUsersPerDay(userService.getNewUsersPerDayThisWeek())  // ✅ AJOUT
                .pricingStats(pricingService.getPricingStats())
                .lastPriceUpdate(pricingService.getLastPriceUpdateDate())
                .anomalyStats(notificationService.getAnomalyStats())
                .recentDisputes(supervisionService.getDisputedRequests(0, 5).getContent())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardApiResponse {
        private AdminDashboardService.AdminDashboardResponse stats;
        private SupervisionStatsDTO supervisionStats;
        private AdminUserService.UserStatsResponse userStats;
        private Long newUsersToday;
        private Long newUsersThisWeek;
        private List<Long> newUsersPerDay;              // ✅ NOUVEAU
        private AdminPricingService.PricingStatsResponse pricingStats;
        private String lastPriceUpdate;
        private AdminAnomalyStatsDTO anomalyStats;
        private List<DisputedRequestDTO> recentDisputes;
    }

    // ==================== GESTION DES UTILISATEURS ====================

    @PutMapping("/factory-users/{id}/assign-recycling-center")
    @ResponseBody
    public ApiResponse<FactoryUserResponseDTO> assignRecyclingCenter(
            @PathVariable Long id,
            @RequestParam Long recyclingCenterId) {
        FactoryUserResponseDTO updated = adminFactoryUserService.assignRecyclingCenter(id, recyclingCenterId);
        return ApiResponse.ok("Usine assignée avec succès", updated);
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Account.RoleType role,
                            @RequestParam(required = false) Account.AccountStatus status,
                            Model model) {
        Page<Account> usersPage = userService.searchUsersWithFiltersEntity(keyword, role, status, page, size);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roleTypes", Account.RoleType.values());
        model.addAttribute("statusTypes", Account.AccountStatus.values());

        return "admin/users";
    }

    @GetMapping("/centers/list")
    @ResponseBody
    public List<Map<String, Object>> getCentersList() {
        return adminRecyclingCenterService.getAllCenters(0, Integer.MAX_VALUE)
                .getContent()
                .stream()
                .map(center -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", center.getId());
                    map.put("name", center.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }


    @PostMapping("/users")
    public String handlePostOnUsers() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/details")
    @ResponseBody
    public AdminUserDetailsDTO getUserDetails(@PathVariable Long id) {
        return adminUserService.getUserFullDetails(id);
    }

    // Dans AdminWebController.java

    @GetMapping("/materials")
    public String materials(Model model) {
        return "admin/materials";
    }

    // ==================== GESTION DES MATÉRIAUX ====================

    @GetMapping("/materials-stats")
    @ResponseBody
    public AdminPricingService.PricingStatsResponse getMaterialsStats() {
        return adminPricingService.getPricingStats();
    }

    @GetMapping("/material/{id}")
    @ResponseBody
    public MaterialResponseDTO getMaterialById(@PathVariable Long id) {
        return adminPricingService.getMaterialById(id);
    }

    @PostMapping("/material-create")
    @ResponseBody
    public ApiResponse<MaterialResponseDTO> createMaterial(@RequestBody AdminPricingService.CreateMaterialRequest request) {
        MaterialResponseDTO created = adminPricingService.createMaterial(request);
        return ApiResponse.ok("Matériau créé avec succès", created);
    }

    @PutMapping("/material-update/{id}")
    @ResponseBody
    public ApiResponse<MaterialResponseDTO> updateMaterial(@PathVariable Long id, @RequestBody AdminPricingService.UpdateMaterialRequest request) {
        MaterialResponseDTO updated = adminPricingService.updateMaterial(id, request);
        return ApiResponse.ok("Matériau mis à jour", updated);
    }

    @PutMapping("/material-price/{id}")
    @ResponseBody
    public ApiResponse<MaterialResponseDTO> updateMaterialPrice(@PathVariable Long id, @RequestBody AdminPricingService.UpdateMaterialPriceRequest request) {
        MaterialResponseDTO updated = adminPricingService.updateMaterialPrice(id, request);
        return ApiResponse.ok("Prix mis à jour", updated);
    }

    @PutMapping("/material-activate/{id}")
    @ResponseBody
    public ApiResponse<MaterialResponseDTO> activateMaterial(@PathVariable Long id) {
        MaterialResponseDTO updated = adminPricingService.activateMaterial(id);
        return ApiResponse.ok("Matériau activé", updated);
    }

    @PutMapping("/material-deactivate/{id}")
    @ResponseBody
    public ApiResponse<MaterialResponseDTO> deactivateMaterial(@PathVariable Long id) {
        MaterialResponseDTO updated = adminPricingService.deactivateMaterial(id);
        return ApiResponse.ok("Matériau désactivé", updated);
    }

    @DeleteMapping("/material-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
        adminPricingService.deleteMaterial(id);
        return ApiResponse.okMessage("Matériau supprimé");
    }

    @GetMapping("/materials-data")
    @ResponseBody
    public Page<MaterialSummaryDTO> getMaterialsData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minCitizenPrice,
            @RequestParam(required = false) BigDecimal maxCitizenPrice,
            @RequestParam(required = false) BigDecimal minCollectorPrice,
            @RequestParam(required = false) BigDecimal maxCollectorPrice) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Si un filtre de statut est demandé (active/inactive), on l’applique en priorité
        if ("active".equals(filter)) {
            return adminPricingService.getActiveMaterials(page, size);
        } else if ("inactive".equals(filter)) {
            return adminPricingService.getInactiveMaterials(page, size);
        } else if (keyword != null || minCitizenPrice != null || maxCitizenPrice != null ||
                minCollectorPrice != null || maxCollectorPrice != null) {
            return adminPricingService.getFilteredMaterials(keyword, minCitizenPrice, maxCitizenPrice,
                    minCollectorPrice, maxCollectorPrice, pageable);
        } else {
            return adminPricingService.getAllMaterials(page, size);
        }
    }

    // Dans AdminWebController.java

    @GetMapping("/materials-list")
    @ResponseBody
    public List<Map<String, Object>> getMaterialsList() {
        List<MaterialSummaryDTO> materials = adminPricingService.getAllMaterials(0, Integer.MAX_VALUE).getContent();
        return materials.stream()
                .filter(m -> m.getIsActive())
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("name", m.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/challenges")
    public String challenges(Model model) {
        return "admin/challenges";
    }

    @GetMapping("/challenges-stats")
    @ResponseBody
    public Map<String, Long> getChallengesStats() {
        long total = challengeRepository.count();
        long active = challengeRepository.countByIsActive(true);
        long inactive = challengeRepository.countByIsActive(false);
        return Map.of("total", total, "active", active, "inactive", inactive);
    }

    @GetMapping("/challenge/{id}")
    @ResponseBody
    public ChallengeResponseDTO getChallengeById(@PathVariable Long id) {
        return adminChallengeService.getChallengeById(id);
    }

    @PostMapping("/challenge-create")
    @ResponseBody
    public ApiResponse<ChallengeResponseDTO> createChallenge(@RequestBody ChallengeRequestDTO request) {
        ChallengeResponseDTO created = adminChallengeService.createChallenge(request);
        return ApiResponse.ok("Défi créé", created);
    }

    @PutMapping("/challenge-update/{id}")
    @ResponseBody
    public ApiResponse<ChallengeResponseDTO> updateChallenge(@PathVariable Long id, @RequestBody ChallengeUpdateDTO request) {
        ChallengeResponseDTO updated = adminChallengeService.updateChallenge(id, request);
        return ApiResponse.ok("Défi mis à jour", updated);
    }

    @PutMapping("/challenge-activate/{id}")
    @ResponseBody
    public ApiResponse<ChallengeResponseDTO> activateChallenge(@PathVariable Long id) {
        ChallengeResponseDTO updated = adminChallengeService.activateChallenge(id);
        return ApiResponse.ok("Défi activé", updated);
    }

    @PutMapping("/challenge-deactivate/{id}")
    @ResponseBody
    public ApiResponse<ChallengeResponseDTO> deactivateChallenge(@PathVariable Long id) {
        ChallengeResponseDTO updated = adminChallengeService.deactivateChallenge(id);
        return ApiResponse.ok("Défi désactivé", updated);
    }

    @DeleteMapping("/challenge-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteChallenge(@PathVariable Long id) {
        adminChallengeService.deleteChallenge(id);
        return ApiResponse.okMessage("Défi supprimé");
    }

    // ==================== GESTION DES DÉFIS ====================
    @GetMapping("/challenges-data")
    @ResponseBody
    public Page<ChallengeSummaryDTO> getChallengesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String challengeType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return adminChallengeService.getFilteredChallenges(keyword, isActive, challengeType, pageable);
    }




    // ==================== GESTION DES COLLECTEURS ====================

    @GetMapping("/collectors")
    public String collectors(Model model) {
        return "admin/collectors";
    }

    @GetMapping("/collectors-stats")
    @ResponseBody
    public AdminCollectorVerificationService.CollectorVerificationStatsResponse getCollectorsStats() {
        return adminCollectorVerificationService.getVerificationStats();
    }

    @GetMapping("/collectors-data")
    @ResponseBody
    public Page<CollectorSummaryDTO> getCollectorsData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filter) { // "verified", "unverified"
        if ("verified".equals(filter)) {
            return adminCollectorVerificationService.getVerifiedCollectors(page, size);
        } else if ("unverified".equals(filter)) {
            return adminCollectorVerificationService.getUnverifiedCollectors(page, size);
        } else {
            return adminCollectorVerificationService.getAllCollectors(page, size);
        }
    }

    @GetMapping("/collector/{id}")
    @ResponseBody
    public AdminCollectorVerificationService.CollectorVerificationDetailResponse getCollectorDetails(@PathVariable Long id) {
        return adminCollectorVerificationService.getCollectorVerificationDetails(id);
    }

    @PutMapping("/collector-verify/{id}")
    @ResponseBody
    public ApiResponse<CollectorResponseDTO> verifyCollector(@PathVariable Long id) {
        return ApiResponse.ok("Collecteur vérifié avec succès", adminCollectorVerificationService.verifyCollector(id));
    }

    @PutMapping("/collector-reject/{id}")
    @ResponseBody
    public ApiResponse<CollectorResponseDTO> rejectCollector(@PathVariable Long id, @RequestBody(required = false) AdminCollectorVerificationService.RejectCollectorRequest request) {
        return ApiResponse.ok("Collecteur rejeté", adminCollectorVerificationService.rejectCollector(id, request));
    }

    @PutMapping("/collector-unverify/{id}")
    @ResponseBody
    public ApiResponse<CollectorResponseDTO> unverifyCollector(@PathVariable Long id) {
        return ApiResponse.ok("Collecteur dévérifié", adminCollectorVerificationService.unverifyCollector(id));
    }

    // ==================== SUPERVISION ====================

    @GetMapping("/supervision")
    public String supervision() {
        return "admin/supervision";
    }

    @GetMapping("/supervision-stats")
    @ResponseBody
    public SupervisionStatsDTO getSupervisionStats() {
        return supervisionService.getSupervisionStats();
    }

    @GetMapping("/supervision-requests")
    @ResponseBody
    public Page<CollectionRequestSupervisionDTO> getSupervisionRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long collectorId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CollectionRequest.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = CollectionRequest.Status.valueOf(status);
        }
        return supervisionService.getAllRequests(statusEnum, clientId, collectorId, materialId, startDate, endDate, page, size);
    }

    @GetMapping("/supervision-disputes")
    @ResponseBody
    public Page<DisputedRequestDTO> getSupervisionDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return supervisionService.getDisputedRequests(page, size);
    }

    @GetMapping("/supervision-request/{requestId}")
    @ResponseBody
    public RequestFullDetailDTO getSupervisionRequestDetail(@PathVariable Long requestId) {
        return supervisionService.getRequestDetail(requestId);
    }

    @PostMapping("/supervision-request/{requestId}/force-cancel")
    @ResponseBody
    public ApiResponse<RequestFullDetailDTO> forceCancelRequest(@PathVariable Long requestId, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("La raison de l'annulation est obligatoire.");
        }
        RequestFullDetailDTO detail = supervisionService.forceCancelRequest(requestId, reason);
        return ApiResponse.ok("Demande annulée avec succès", detail);
    }

    @PostMapping("/supervision-request/{requestId}/reassign")
    @ResponseBody
    public ApiResponse<RequestFullDetailDTO> reassignRequest(@PathVariable Long requestId, @RequestBody Map<String, Long> body) {
        Long collectorId = body.get("collectorId");
        if (collectorId == null) {
            throw new BadRequestException("L'identifiant du collecteur est obligatoire.");
        }
        RequestFullDetailDTO detail = supervisionService.reassignRequest(requestId, collectorId);
        return ApiResponse.ok("Demande réassignée avec succès", detail);
    }

// ==================== GESTION SUPPORT ====================

    // ==================== SUPPORT ====================

    @GetMapping("/support")
    public String support() {
        return "admin/support";
    }

    @GetMapping("/support-stats")
    @ResponseBody
    public AdminSupportStatsDTO getSupportStats() {
        return adminSupportService.getSupportStats();
    }

    @GetMapping("/support-tickets")
    @ResponseBody
    public Page<SupportTicketSummaryDTO> getSupportTickets(
            @RequestParam(required = false) SupportTicket.Status status,
            @RequestParam(required = false) SupportTicket.RoleType roleType,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminSupportService.getAllTickets(status, roleType, accountId, page, size);
    }

    @GetMapping("/support-ticket/{id}")
    @ResponseBody
    public SupportTicketResponseDTO getSupportTicket(@PathVariable Long id) {
        return adminSupportService.getTicketDetail(id);
    }

    @PostMapping("/support-ticket/{id}/respond")
    @ResponseBody
    public ApiResponse<SupportTicketResponseDTO> respondToTicket(@PathVariable Long id, @RequestBody AdminTicketResponseRequestDTO request) {
        return ApiResponse.ok("Réponse envoyée", adminSupportService.respondToTicket(id, request));
    }

    @PostMapping("/support-ticket/{id}/assign")
    @ResponseBody
    public ApiResponse<SupportTicketResponseDTO> assignTicket(@PathVariable Long id, @RequestBody AdminTicketAssignRequestDTO request) {
        return ApiResponse.ok("Ticket assigné", adminSupportService.assignTicket(id, request));
    }

    @PostMapping("/support-ticket/{id}/close")
    @ResponseBody
    public ApiResponse<SupportTicketResponseDTO> closeTicket(@PathVariable Long id) {
        return ApiResponse.ok("Ticket fermé", adminSupportService.closeTicket(id));
    }

    @PostMapping("/support-ticket/{id}/reopen")
    @ResponseBody
    public ApiResponse<SupportTicketResponseDTO> reopenTicket(@PathVariable Long id) {
        return ApiResponse.ok("Ticket rouvert", adminSupportService.reopenTicket(id));
    }

    @GetMapping("/support-faqs")
    @ResponseBody
    public Page<FaqEntrySummaryDTO> getSupportFaqs(
            @RequestParam(required = false) FaqEntry.RoleType roleType,
            @RequestParam(required = false) FaqEntry.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminSupportService.getAllFaqs(roleType, status, page, size);
    }

    @GetMapping("/support-faq/{id}")
    @ResponseBody
    public FaqEntryResponseDTO getSupportFaq(@PathVariable Long id) {
        return adminSupportService.getFaqDetail(id);
    }

    @PostMapping("/support-faq")
    @ResponseBody
    public ApiResponse<FaqEntryResponseDTO> createSupportFaq(@RequestBody FaqEntryRequestDTO request) {
        return ApiResponse.ok("FAQ créée", adminSupportService.createFaq(request));
    }

    @PutMapping("/support-faq/{id}")
    @ResponseBody
    public ApiResponse<FaqEntryResponseDTO> updateSupportFaq(@PathVariable Long id, @RequestBody FaqEntryUpdateDTO request) {
        return ApiResponse.ok("FAQ mise à jour", adminSupportService.updateFaq(id, request));
    }

    @PutMapping("/support-faq/{id}/activate")
    @ResponseBody
    public ApiResponse<FaqEntryResponseDTO> activateSupportFaq(@PathVariable Long id) {
        return ApiResponse.ok("FAQ activée", adminSupportService.activateFaq(id));
    }

    @PutMapping("/support-faq/{id}/deactivate")
    @ResponseBody
    public ApiResponse<FaqEntryResponseDTO> deactivateSupportFaq(@PathVariable Long id) {
        return ApiResponse.ok("FAQ désactivée", adminSupportService.deactivateFaq(id));
    }

    @DeleteMapping("/support-faq/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteSupportFaq(@PathVariable Long id) {
        adminSupportService.deleteFaq(id);
        return ApiResponse.okMessage("FAQ supprimée");
    }

// ==================== GESTION NOTIFICATIONS ====================

    @GetMapping("/notifications")
    public String notifications() {
        return "admin/notifications";
    }

    @GetMapping("/notifications-stats")
    @ResponseBody
    public AdminNotificationStatsDTO getNotificationStats() {
        return adminNotificationAndLogService.getNotificationStats();
    }

    @PostMapping("/notifications/send")
    @ResponseBody
    public ApiResponse<List<NotificationResponseDTO>> sendNotification(@RequestBody AdminNotificationSendRequestDTO request) {
        return ApiResponse.ok("Notification(s) envoyée(s)", adminNotificationAndLogService.sendTargetedNotification(request));
    }

    @PostMapping("/notifications/broadcast")
    @ResponseBody
    public ApiResponse<Map<String, Object>> sendBroadcast(@RequestBody AdminNotificationSendRequestDTO request) {
        return ApiResponse.ok("Annonce broadcast envoyée", adminNotificationAndLogService.sendBroadcastAnnouncement(request));
    }

    @GetMapping("/notifications-history")
    @ResponseBody
    public Page<NotificationSummaryDTO> getNotificationHistory(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminNotificationAndLogService.getNotificationHistory(type, accountId, page, size);
    }

    @GetMapping("/notifications-history/role/{roleType}")
    @ResponseBody
    public Page<NotificationSummaryDTO> getNotificationHistoryByRole(
            @PathVariable Account.RoleType roleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminNotificationAndLogService.getNotificationHistoryByRole(roleType, page, size);
    }

// ==================== GESTION ANOMALIES ====================

    @GetMapping("/anomalies")
    public String anomalies() {
        return "admin/anomalies";
    }

    @GetMapping("/anomalies-stats")
    @ResponseBody
    public AdminAnomalyStatsDTO getAnomalyStats() {
        return adminNotificationAndLogService.getAnomalyStats();
    }

    @GetMapping("/anomalies-list")
    @ResponseBody
    public Page<AdminAnomalyDTO> getAnomalies(
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminNotificationAndLogService.getAnomalies(severity, page, size);
    }

    @GetMapping("/sensitive-actions")
    @ResponseBody
    public List<AdminAnomalyDTO> getSensitiveActions() {
        return adminNotificationAndLogService.getSensitiveActions();
    }

    @GetMapping("/supervision/deliveries")
    @ResponseBody
    public Page<DeliveryValidationDTO> getDeliveriesWithValidation(
            @RequestParam(required = false) FactoryDelivery.DeliveryStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long collectorId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return supervisionService.getDeliveriesWithValidation(status, clientId, collectorId, materialId, page, size);
    }


    // ==================== GESTION DES MACHINES ====================

    @GetMapping("/machines")
    public String machines(Model model) {
        return "admin/machines";
    }

    @GetMapping("/machines-stats")
    @ResponseBody
    public Map<String, Long> getMachinesStats() {
        long total = machineRepository.count();
        long working = machineRepository.countByStatus(Machine.MachineStatus.WORKING);
        long maintenance = machineRepository.countByStatus(Machine.MachineStatus.MAINTENANCE);
        long outOfService = machineRepository.countByStatus(Machine.MachineStatus.OUT_OF_SERVICE);
        return Map.of("total", total, "working", working, "maintenance", maintenance, "outOfService", outOfService);
    }

    @GetMapping("/machines-data")
    @ResponseBody
    public Page<MachineSummaryDTO> getMachinesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String serialNumber,   // ✅ NOUVEAU
            @RequestParam(required = false) String status) {
        return adminMachineService.getFilteredMachines(name, serialNumber, status, page, size);
    }

    @GetMapping("/machine/{id}")
    @ResponseBody
    public MachineResponseDTO getMachineById(@PathVariable Long id) {
        return adminMachineService.getMachineById(id);
    }

    @PostMapping(value = "/machine-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse<MachineResponseDTO> createMachine(
            @RequestPart("name") String name,
            @RequestPart("serialNumber") String serialNumber,
            @RequestPart(value = "status", required = false) String status,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        MachineRequestDTO request = new MachineRequestDTO();
        request.setName(name);
        request.setSerialNumber(serialNumber);
        if (status != null) request.setStatus(Machine.MachineStatus.valueOf(status));
        request.setDescription(description);
        MachineResponseDTO created = adminMachineService.createMachine(request, photo);
        return ApiResponse.ok("Machine créée avec succès", created);
    }

    @PutMapping(value = "/machine-update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse<MachineResponseDTO> updateMachine(
            @PathVariable Long id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "serialNumber", required = false) String serialNumber,
            @RequestPart(value = "status", required = false) String status,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        MachineUpdateDTO request = new MachineUpdateDTO();
        request.setName(name);
        request.setSerialNumber(serialNumber);
        if (status != null) request.setStatus(Machine.MachineStatus.valueOf(status));
        request.setDescription(description);
        MachineResponseDTO updated = adminMachineService.updateMachine(id, request, photo);
        return ApiResponse.ok("Machine mise à jour", updated);
    }

    @DeleteMapping("/machine-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteMachine(@PathVariable Long id) {
        adminMachineService.deleteMachine(id);
        return ApiResponse.okMessage("Machine supprimée");
    }

// ==================== GESTION DES INCIDENTS ====================

    @GetMapping("/incidents-data")
    @ResponseBody
    public Page<MachineIncidentSummaryDTO> getIncidentsData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status) {
        return adminMachineService.getAllIncidents(page, size, severity, status);
    }

    @GetMapping("/incident/{id}")
    @ResponseBody
    public MachineIncidentResponseDTO getIncidentById(@PathVariable Long id) {
        return adminMachineService.getIncidentById(id);
    }

    @PutMapping("/incident/{id}/status")
    @ResponseBody
    public ApiResponse<MachineIncidentResponseDTO> updateIncidentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        MachineIncidentResponseDTO updated = adminMachineService.updateIncidentStatus(id, newStatus);
        return ApiResponse.ok("Statut mis à jour", updated);
    }

    @DeleteMapping("/incident/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteIncident(@PathVariable Long id) {
        adminMachineService.deleteIncident(id);
        return ApiResponse.okMessage("Incident supprimé");
    }

    // ==================== GESTION DES USINES ====================

    @GetMapping("/centers")
    public String centers(Model model) {
        return "admin/centers";
    }

    @GetMapping("/center/{id}")
    @ResponseBody
    public RecyclingCenterResponseDTO getCenterById(@PathVariable Long id) {
        return adminRecyclingCenterService.getCenterById(id);
    }

    @GetMapping("/center/{id}/stats")
    @ResponseBody
    public AdminRecyclingCenterService.RecyclingCenterStatsDTO getCenterStats(@PathVariable Long id) {
        return adminRecyclingCenterService.getCenterStats(id);
    }

    @PostMapping("/center-create")
    @ResponseBody
    public ApiResponse<RecyclingCenterResponseDTO> createCenter(@RequestBody RecyclingCenterRequestDTO request) {
        RecyclingCenterResponseDTO created = adminRecyclingCenterService.createCenter(request);
        return ApiResponse.ok("Centre créé avec succès", created);
    }

    @PutMapping("/center-update/{id}")
    @ResponseBody
    public ApiResponse<RecyclingCenterResponseDTO> updateCenter(@PathVariable Long id, @RequestBody RecyclingCenterUpdateDTO request) {
        RecyclingCenterResponseDTO updated = adminRecyclingCenterService.updateCenter(id, request);
        return ApiResponse.ok("Centre mis à jour", updated);
    }

    @DeleteMapping("/center-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteCenter(@PathVariable Long id) {
        adminRecyclingCenterService.deleteCenter(id);
        return ApiResponse.okMessage("Centre supprimé");
    }

    @GetMapping("/centers-data")
    @ResponseBody
    public Page<RecyclingCenterSummaryDTO> getCentersData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) BigDecimal minCapacity,
            @RequestParam(required = false) BigDecimal maxCapacity) {
        return adminRecyclingCenterService.getFilteredCenters(
                name, location, email, phone, minCapacity, maxCapacity, page, size);
    }

    //. ==================== UTILISATEURS PAR CATÉGORIE ====================
    @GetMapping("/users/clients")
    @ResponseBody
    public Page<AccountSummaryDTO> getClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Account.AccountStatus status,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints) {
        return adminUserService.getClients(page, size, keyword, status, minPoints, maxPoints);
    }

    @GetMapping("/users/collectors")
    @ResponseBody
    public Page<AccountSummaryDTO> getCollectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Account.AccountStatus status,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) BigDecimal minRating) {
        return adminUserService.getCollectors(page, size, keyword, status, verified, minRating);
    }

    @GetMapping("/users/accountants")
    @ResponseBody
    public Page<AccountSummaryDTO> getAccountants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Account.AccountStatus status,
            @RequestParam(required = false) Long centerId) {
        return adminUserService.getFactoryUsersByPosition(page, size, keyword, status, FactoryUser.FactoryPosition.ACCOUNTANT, centerId);
    }

    @GetMapping("/users/managers")
    @ResponseBody
    public Page<AccountSummaryDTO> getManagers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Account.AccountStatus status,
            @RequestParam(required = false) Long centerId) {
        return adminUserService.getFactoryUsersByPosition(page, size, keyword, status, FactoryUser.FactoryPosition.MANAGER, centerId);
    }

    @GetMapping("/users/admins")
    @ResponseBody
    public Page<AccountSummaryDTO> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Account.AccountStatus status,
            @RequestParam(required = false) Long centerId) {
        return adminUserService.getFactoryUsersByPosition(page, size, keyword, status, FactoryUser.FactoryPosition.ADMIN, centerId);
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ApiResponse<AccountResponseDTO> updateUser(@PathVariable Long id, @RequestBody AccountUpdateDTO dto) {
        return ApiResponse.ok("Utilisateur mis à jour", adminUserService.updateUser(id, dto));
    }




    //***********************
    // ==================== ACTIONS UTILISATEURS (retour JSON) ====================

    @PostMapping("/users/{id}/activate")
    @ResponseBody
    public ApiResponse<Void> activateUser(@PathVariable Long id) {
        adminUserService.activateUser(id);
        return ApiResponse.okMessage("Utilisateur activé avec succès.");
    }

    @PostMapping("/users/{id}/deactivate")
    @ResponseBody
    public ApiResponse<Void> deactivateUser(@PathVariable Long id) {
        adminUserService.deactivateUser(id);
        return ApiResponse.okMessage("Utilisateur désactivé avec succès.");
    }

    @PostMapping("/users/{id}/delete")
    @ResponseBody
    public ApiResponse<Void> softDeleteUser(@PathVariable Long id) {
        adminUserService.softDeleteUser(id);
        return ApiResponse.okMessage("Utilisateur supprimé logiquement.");
    }

    @PostMapping("/users/{id}/reset-password")
    @ResponseBody
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        String confirmPassword = payload.get("confirmPassword");
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Les mots de passe ne correspondent pas.");
        }
        if (newPassword.length() < 8) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        adminUserService.resetUserPassword(id, newPassword);
        return ApiResponse.okMessage("Mot de passe réinitialisé avec succès.");
    }

// ==================== AJOUT COMPTABLE / MANAGER (retour JSON) ====================

    @PostMapping("/add-accountant")
    @ResponseBody
    public ApiResponse<FactoryUserResponseDTO> addAccountant(@RequestBody AdminFactoryUserService.CreateFactoryUserRequest request) {
        FactoryUserResponseDTO created = adminFactoryUserService.createAccountant(request, request.getRecyclingCenterId());
        return ApiResponse.ok("Comptable ajouté avec succès.", created);
    }

    @PostMapping("/add-manager")
    @ResponseBody
    public ApiResponse<FactoryUserResponseDTO> addManager(@RequestBody AdminFactoryUserService.CreateFactoryUserRequest request) {
        FactoryUserResponseDTO created = adminFactoryUserService.createWorkshopManager(request, request.getRecyclingCenterId());
        return ApiResponse.ok("Manager ajouté avec succès.", created);
    }


    //*****************
    // Dans AdminWebController.java, ajoutez cette méthode après les autres endpoints

    @PutMapping("/factory-users/{factoryUserId}")
    @ResponseBody
    public ApiResponse<FactoryUserResponseDTO> updateFactoryUserWeb(
            @PathVariable Long factoryUserId,
            @RequestBody Map<String, Object> payload) {

        String firstName = (String) payload.get("firstName");
        String lastName = (String) payload.get("lastName");
        Boolean isHeadAccountant = (Boolean) payload.get("isHeadAccountant");

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new BadRequestException("Le prénom et le nom sont obligatoires.");
        }

        AdminFactoryUserService.UpdateFactoryUserRequest request = new AdminFactoryUserService.UpdateFactoryUserRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setIsHeadAccountant(isHeadAccountant);

        FactoryUserResponseDTO updated = adminFactoryUserService.updateFactoryUser(factoryUserId, request);
        return ApiResponse.ok("Employé mis à jour", updated);
    }




    // Dans AdminWebController
    @PutMapping("/factory-users/{factoryUserId}/head-accountant")
    @ResponseBody
    public ApiResponse<Void> setHeadAccountant(@PathVariable Long factoryUserId, @RequestBody Map<String, Boolean> payload) {
        Boolean isHead = payload.get("isHeadAccountant");
        if (isHead == null) throw new BadRequestException("isHeadAccountant requis");
        AdminFactoryUserService.UpdateFactoryUserRequest req = new AdminFactoryUserService.UpdateFactoryUserRequest();
        req.setIsHeadAccountant(isHead);
        adminFactoryUserService.updateFactoryUser(factoryUserId, req);
        return ApiResponse.okMessage("Statut chef comptable mis à jour");
    }

    //*****************************************

    // Dans AdminWebController, injecter le repository
    private final CancellationLogRepository cancellationLogRepository;

    // Dans AdminWebController.java

    @GetMapping("/cancellations")
    public String cancellationsPage() {
        return "admin/cancellations";
    }


    // DTO simple (statique interne)
    public static class RiskyAccountDTO {
        public Long id;
        public String roleType;
        public String fullName;
        public String email;
        public long cancelCount7d;
        public String status;
        public RiskyAccountDTO(Long id, Account.RoleType roleType, String fullName, String email, long cancelCount7d, String status) {
            this.id = id;
            this.roleType = roleType.name();
            this.fullName = fullName;
            this.email = email;
            this.cancelCount7d = cancelCount7d;
            this.status = status;
        }
    }

    private String getFullName(Account acc) {
        if (acc.getClient() != null) return acc.getClient().getFirstName() + " " + acc.getClient().getLastName();
        if (acc.getCollector() != null) return acc.getCollector().getFirstName() + " " + acc.getCollector().getLastName();
        if (acc.getFactoryUser() != null) return acc.getFactoryUser().getFirstName() + " " + acc.getFactoryUser().getLastName();
        return acc.getEmail();
    }

    @GetMapping("/users/{id}/cancellations")
    @ResponseBody
    public ApiResponse<Map<String, Object>> getUserCancellations(@PathVariable Long id) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        long count7d = cancellationLogRepository.countByAccountIdAndCancelledAtAfter(id, since);
        List<CancellationLog> recentLogs = cancellationLogRepository.findByAccountIdOrderByCancelledAtDesc(id)
                .stream().limit(10).toList();

        Account account = accountRepository.findById(id).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("last7DaysCount", count7d);
        result.put("recentLogs", recentLogs);
        result.put("id", id);
        result.put("fullName", account != null ? getFullName(account) : "");
        result.put("roleType", account != null ? account.getRoleType().name() : "");
        result.put("status", account != null ? account.getStatus().name() : "");
        return ApiResponse.ok(result);
    }


    // Ajouter l'injection en haut de la classe (dans les champs)
    private final CancellationMonitoringService cancellationMonitoringService;

    // Et la méthode
    @PostMapping("/users/{id}/reactivate")
    @ResponseBody
    public ApiResponse<Void> reactivateUser(@PathVariable Long id) {
        cancellationMonitoringService.reactivateAccount(id);
        return ApiResponse.okMessage("Compte réactivé avec succès.");
    }

    @GetMapping("/api/risky-accounts")
    @ResponseBody
    public ApiResponse<List<RiskyAccountDTO>> getRiskyAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String status
    ) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Account> allAccounts = accountRepository.findAll();
        List<RiskyAccountDTO> risky = new ArrayList<>();

        for (Account acc : allAccounts) {
            if (acc.getRoleType() == Account.RoleType.CLIENT || acc.getRoleType() == Account.RoleType.COLLECTOR) {

                // Filtre par mot-clé (nom ou email)
                if (keyword != null && !keyword.isBlank()) {
                    String fullName = getFullName(acc).toLowerCase();
                    String email = acc.getEmail().toLowerCase();
                    String kw = keyword.toLowerCase();
                    if (!fullName.contains(kw) && !email.contains(kw)) {
                        continue;
                    }
                }

                // Filtre par type (CLIENT / COLLECTOR)
                if (roleType != null && !roleType.isBlank()) {
                    if (!acc.getRoleType().name().equals(roleType)) {
                        continue;
                    }
                }

                // Filtre par statut du compte (ACTIVE / INACTIVE / DELETED)
                if (status != null && !status.isBlank()) {
                    if (!acc.getStatus().name().equals(status)) {
                        continue;
                    }
                }

                long cancelCount = cancellationLogRepository.countByAccountIdAndCancelledAtAfter(acc.getId(), since);
                int displayThreshold = 1; // affiche dès 1 annulation
                if (cancelCount >= displayThreshold) {
                    risky.add(new RiskyAccountDTO(
                            acc.getId(),
                            acc.getRoleType(),
                            getFullName(acc),
                            acc.getEmail(),
                            cancelCount,
                            acc.getStatus().name()
                    ));
                }
            }
        }
        return ApiResponse.ok(risky);
    }

    //**************************************************


}