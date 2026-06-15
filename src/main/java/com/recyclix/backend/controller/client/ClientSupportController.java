//package com.recyclix.backend.controller.client;
//
//import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
//import com.recyclix.backend.dto.notification.NotificationResponseDTO;
//import com.recyclix.backend.dto.notification.NotificationSummaryDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
//import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
//import com.recyclix.backend.service.client.ClientSupportService;
//import com.recyclix.backend.util.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/client/support")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('CLIENT')")
//@Validated
//public class ClientSupportController {
//
//    private final ClientSupportService clientSupportService;
//
//    //. -------------------- CREATE TICKET -------------------- .\\
//    @PostMapping("/tickets")
//    public ApiResponse<SupportTicketResponseDTO> createTicket(
//            @Valid @RequestBody SupportTicketRequestDTO request
//    ) {
//        return ApiResponse.ok(
//                "Ticket support créé avec succès.",
//                clientSupportService.createTicket(request)
//        );
//    }
//
//    //. -------------------- MY TICKETS -------------------- .\\
//    @GetMapping("/tickets")
//    public ApiResponse<List<SupportTicketSummaryDTO>> getMyTickets() {
//        return ApiResponse.ok(
//                "Mes tickets récupérés avec succès.",
//                clientSupportService.getMyTickets()
//        );
//    }
//
//    //. -------------------- MY TICKET BY ID -------------------- .\\
//    @GetMapping("/tickets/{ticketId}")
//    public ApiResponse<SupportTicketResponseDTO> getMyTicketById(
//            @PathVariable Long ticketId
//    ) {
//        return ApiResponse.ok(
//                "Ticket récupéré avec succès.",
//                clientSupportService.getMyTicketById(ticketId)
//        );
//    }
//
//    //. -------------------- MY TICKET RESPONSE -------------------- .\\
//    @GetMapping("/tickets/{ticketId}/response")
//    public ApiResponse<ClientSupportService.TicketResponseView> getTicketResponse(
//            @PathVariable Long ticketId
//    ) {
//        return ApiResponse.ok(
//                "Réponse du ticket récupérée avec succès.",
//                clientSupportService.getTicketResponse(ticketId)
//        );
//    }
//
//    //. -------------------- MY NOTIFICATIONS -------------------- .\\
//    @GetMapping("/notifications")
//    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotifications() {
//        List<NotificationResponseDTO> notifications = clientSupportService.getMyNotifications();
//        return ResponseEntity.ok(
//                ApiResponse.ok("Notifications récupérées avec succès.", notifications)
//        );
//    }
//
//    //. -------------------- CLIENT FAQ -------------------- .\\
//    @GetMapping("/faq")
//    public ApiResponse<List<FaqEntrySummaryDTO>> getClientFaq() {
//        return ApiResponse.ok(
//                "FAQ client récupérée avec succès.",
//                clientSupportService.getClientFaq()
//        );
//    }
//
//    // Dans ClientSupportController.java
//
//    @PutMapping("/notifications/{notificationId}/read")
//    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long notificationId) {
//        clientSupportService.markNotificationAsRead(notificationId);
//        return ResponseEntity.ok(ApiResponse.okMessage("Notification marquée comme lue"));
//    }
//
//    @PutMapping("/notifications/read-all")
//    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead() {
//        clientSupportService.markAllNotificationsAsRead();
//        return ResponseEntity.ok(ApiResponse.okMessage("Toutes les notifications ont été marquées comme lues"));
//    }
//}







package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.service.client.ClientSupportService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientSupportController {

    private final ClientSupportService clientSupportService;

    @PostMapping("/tickets")
    public ApiResponse<SupportTicketResponseDTO> createTicket(@Valid @RequestBody SupportTicketRequestDTO request) {
        return ApiResponse.ok("Ticket créé", clientSupportService.createTicket(request));
    }

    @GetMapping("/tickets")
    public ApiResponse<List<SupportTicketSummaryDTO>> getMyTickets() {
        return ApiResponse.ok("Mes tickets", clientSupportService.getMyTickets());
    }

    @GetMapping("/tickets/{ticketId}")
    public ApiResponse<SupportTicketResponseDTO> getMyTicketById(@PathVariable Long ticketId) {
        return ApiResponse.ok("Ticket", clientSupportService.getMyTicketById(ticketId));
    }

    @GetMapping("/tickets/{ticketId}/response")
    public ApiResponse<ClientSupportService.TicketResponseView> getTicketResponse(@PathVariable Long ticketId) {
        return ApiResponse.ok("Réponse", clientSupportService.getTicketResponse(ticketId));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.ok("Notifications", clientSupportService.getMyNotifications()));
    }

    @GetMapping("/faq")
    public ApiResponse<List<FaqEntrySummaryDTO>> getClientFaq() {
        return ApiResponse.ok("FAQ", clientSupportService.getClientFaq());
    }

    // 🔥 NOUVEAUX ENDPOINTS POUR MARQUER COMME LU
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long notificationId) {
        clientSupportService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.okMessage("Notification marquée comme lue"));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead() {
        clientSupportService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ApiResponse.okMessage("Toutes les notifications ont été marquées comme lues"));
    }
}