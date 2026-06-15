// recyclix\backend\controller\admin\AdminNotificationAndLogController.java
package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.notification.NotificationSummaryDTO;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Notification.NotificationType;
import com.recyclix.backend.service.admin.AdminNotificationAndLogService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACTORY_USER') and @factoryAccess.hasPosition('ADMIN')")
public class AdminNotificationAndLogController {

    private final AdminNotificationAndLogService notificationService;

    // ============================================================
    // STATISTIQUES
    // ============================================================
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminNotificationStatsDTO>> getNotificationStats() {
        AdminNotificationStatsDTO stats = notificationService.getNotificationStats();
        return ResponseEntity.ok(
                ApiResponse.ok("Statistiques des notifications récupérées avec succès.", stats)
        );
    }

    // ============================================================
    // ENVOYER UNE NOTIFICATION CIBLÉE
    // ============================================================
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> sendTargetedNotification(
            @Valid @RequestBody AdminNotificationSendRequestDTO request
    ) {
        List<NotificationResponseDTO> notifications = notificationService.sendTargetedNotification(request);
        return ResponseEntity.ok(
                ApiResponse.ok(
                        notifications.size() + " notification(s) envoyée(s) avec succès.",
                        notifications
                )
        );
    }

    // ============================================================
    // ENVOYER UNE ANNONCE BROADCAST
    // ============================================================
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendBroadcastAnnouncement(
            @Valid @RequestBody AdminNotificationSendRequestDTO request
    ) {
        Map<String, Object> result = notificationService.sendBroadcastAnnouncement(request);
        return ResponseEntity.ok(
                ApiResponse.ok("Annonce broadcast envoyée avec succès.", result)
        );
    }

    // ============================================================
    // HISTORIQUE DES NOTIFICATIONS
    // ============================================================
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<NotificationSummaryDTO>>> getNotificationHistory(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationSummaryDTO> history = notificationService.getNotificationHistory(
                type, accountId, page, size
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Historique des notifications récupéré avec succès.", history)
        );
    }

    // ============================================================
    // STATISTIQUES DES ANOMALIES
    // ============================================================
    @GetMapping("/anomalies/stats")
    public ResponseEntity<ApiResponse<AdminAnomalyStatsDTO>> getAnomalyStats() {
        AdminAnomalyStatsDTO stats = notificationService.getAnomalyStats();
        return ResponseEntity.ok(
                ApiResponse.ok("Statistiques des anomalies récupérées avec succès.", stats)
        );
    }

    // ============================================================
    // LISTE DES ANOMALIES
    // ============================================================
    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<Page<AdminAnomalyDTO>>> getAnomalies(
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminAnomalyDTO> anomalies = notificationService.getAnomalies(severity, page, size);
        return ResponseEntity.ok(
                ApiResponse.ok("Anomalies détectées avec succès.", anomalies)
        );
    }

    // ============================================================
    // LOGS DES ACTIONS SENSIBLES
    // ============================================================
    @GetMapping("/sensitive-actions")
    public ResponseEntity<ApiResponse<List<AdminAnomalyDTO>>> getSensitiveActions() {
        List<AdminAnomalyDTO> actions = notificationService.getSensitiveActions();
        return ResponseEntity.ok(
                ApiResponse.ok("Actions sensibles récupérées avec succès.", actions)
        );
    }



    // recyclix\backend\controller\admin\AdminNotificationAndLogController.java

    // ============================================================
// HISTORIQUE DES NOTIFICATIONS PAR RÔLE
// ============================================================
    @GetMapping("/history/role/{roleType}")
    public ResponseEntity<ApiResponse<Page<NotificationSummaryDTO>>> getNotificationHistoryByRole(
            @PathVariable Account.RoleType roleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationSummaryDTO> history = notificationService.getNotificationHistoryByRole(
                roleType, page, size
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Historique des notifications pour le rôle " + roleType + " récupéré avec succès.",
                        history
                )
        );
    }
}