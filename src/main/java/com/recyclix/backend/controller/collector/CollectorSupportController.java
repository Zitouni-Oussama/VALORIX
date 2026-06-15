//package com.recyclix.backend.controller.collector;
//
//import com.recyclix.backend.dto.notification.NotificationResponseDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
//import com.recyclix.backend.model.Account;
//import com.recyclix.backend.model.Notification;
//import com.recyclix.backend.service.collector.CollectorNotificationService;
//import com.recyclix.backend.service.collector.CollectorSupportService;
//import com.recyclix.backend.util.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/collector/support")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('COLLECTOR')")
//public class CollectorSupportController {
//
//    private final CollectorSupportService collectorSupportService;
//    private final CollectorNotificationService notificationService;
//
//    //. -------------------- CREATE TICKET -------------------- .\\
//    @PostMapping("/tickets")
//    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> createTicket(
//            @Valid @RequestBody SupportTicketRequestDTO dto
//    ) {
//        return ResponseEntity.ok(
//                ApiResponse.ok(
//                        "Ticket support créé avec succès.",
//                        collectorSupportService.createTicket(dto)
//                )
//        );
//    }
//
//    //. -------------------- GET MY TICKETS -------------------- .\\
//    @GetMapping("/tickets")
//    public ResponseEntity<ApiResponse<List<SupportTicketSummaryDTO>>> getMyTickets() {
//        return ResponseEntity.ok(
//                ApiResponse.ok(
//                        "Tickets support récupérés avec succès.",
//                        collectorSupportService.getMyTickets()
//                )
//        );
//    }
//
//    //. -------------------- GET MY TICKET BY ID -------------------- .\\
//    @GetMapping("/tickets/{ticketId}")
//    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> getMyTicketById(
//            @PathVariable Long ticketId
//    ) {
//        return ResponseEntity.ok(
//                ApiResponse.ok(
//                        "Détail du ticket récupéré avec succès.",
//                        collectorSupportService.getMyTicketById(ticketId)
//                )
//        );
//    }
//
//    @GetMapping("/notifications")
//    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotifications() {
//        List<NotificationResponseDTO> notifications = notificationService.getMyNotifications();
//        return ResponseEntity.ok(
//                ApiResponse.ok("Notifications récupérées avec succès.", notifications)
//        );
//    }
//
//    // Dans CollectorSupportController.java
//
//    @PutMapping("/notifications/{notificationId}/read")
//    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long notificationId) {
//        notificationService.markNotificationAsRead(notificationId);
//        return ResponseEntity.ok(ApiResponse.okMessage("Notification marquée comme lue"));
//    }
//
//    @PutMapping("/notifications/read-all")
//    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead() {
//        notificationService.markAllNotificationsAsRead();
//        return ResponseEntity.ok(ApiResponse.okMessage("Toutes les notifications ont été marquées comme lues"));
//    }
//}












package com.recyclix.backend.controller.collector;

import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.service.collector.CollectorNotificationService;
import com.recyclix.backend.service.collector.CollectorSupportService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collector/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorSupportController {

    private final CollectorSupportService collectorSupportService;
    private final CollectorNotificationService notificationService;

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> createTicket(@Valid @RequestBody SupportTicketRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok("Ticket créé", collectorSupportService.createTicket(dto)));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<List<SupportTicketSummaryDTO>>> getMyTickets() {
        return ResponseEntity.ok(ApiResponse.ok("Mes tickets", collectorSupportService.getMyTickets()));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> getMyTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.ok("Détail ticket", collectorSupportService.getMyTicketById(ticketId)));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.ok("Notifications", notificationService.getMyNotifications()));
    }

    // 🔥 NOUVEAUX ENDPOINTS POUR MARQUER COMME LU
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.okMessage("Notification marquée comme lue"));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead() {
        notificationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ApiResponse.okMessage("Toutes les notifications ont été marquées comme lues"));
    }
}