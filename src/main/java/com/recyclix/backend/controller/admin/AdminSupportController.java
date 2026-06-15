// recyclix\backend\controller\admin\AdminSupportController.java
package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.admin.AdminSupportStatsDTO;
import com.recyclix.backend.dto.admin.AdminTicketAssignRequestDTO;
import com.recyclix.backend.dto.admin.AdminTicketResponseRequestDTO;
import com.recyclix.backend.dto.faq_entry.*;
import com.recyclix.backend.dto.support_ticket.*;
import com.recyclix.backend.model.FaqEntry;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.service.admin.AdminSupportService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACTORY_USER') and @factoryAccess.hasPosition('ADMIN')")
public class AdminSupportController {

    private final AdminSupportService supportService;

    // ============================================================
    // STATISTIQUES
    // ============================================================
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminSupportStatsDTO>> getSupportStats() {
        AdminSupportStatsDTO stats = supportService.getSupportStats();
        return ResponseEntity.ok(
                ApiResponse.ok("Statistiques du support récupérées avec succès.", stats)
        );
    }

    // ============================================================
    // TICKETS — LISTER
    // ============================================================
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketSummaryDTO>>> getAllTickets(
            @RequestParam(required = false) SupportTicket.Status status,
            @RequestParam(required = false) SupportTicket.RoleType roleType,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<SupportTicketSummaryDTO> tickets = supportService.getAllTickets(
                status, roleType, accountId, page, size
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Tickets récupérés avec succès.", tickets)
        );
    }

    // ============================================================
    // TICKETS — DÉTAIL
    // ============================================================
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> getTicketDetail(
            @PathVariable Long ticketId
    ) {
        SupportTicketResponseDTO ticket = supportService.getTicketDetail(ticketId);
        return ResponseEntity.ok(
                ApiResponse.ok("Détail du ticket récupéré avec succès.", ticket)
        );
    }

    // ============================================================
    // TICKETS — RÉPONDRE
    // ============================================================
    @PutMapping("/tickets/{ticketId}/respond")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> respondToTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AdminTicketResponseRequestDTO request
    ) {
        SupportTicketResponseDTO ticket = supportService.respondToTicket(ticketId, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Réponse envoyée avec succès.", ticket)
        );
    }

    // ============================================================
    // TICKETS — ASSIGNER
    // ============================================================
    @PutMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AdminTicketAssignRequestDTO request
    ) {
        SupportTicketResponseDTO ticket = supportService.assignTicket(ticketId, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Ticket assigné avec succès.", ticket)
        );
    }

    // ============================================================
    // TICKETS — FERMER
    // ============================================================
    @PutMapping("/tickets/{ticketId}/close")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> closeTicket(
            @PathVariable Long ticketId
    ) {
        SupportTicketResponseDTO ticket = supportService.closeTicket(ticketId);
        return ResponseEntity.ok(
                ApiResponse.ok("Ticket fermé avec succès.", ticket)
        );
    }

    // ============================================================
    // TICKETS — RÉOUVRIR
    // ============================================================
    @PutMapping("/tickets/{ticketId}/reopen")
    public ResponseEntity<ApiResponse<SupportTicketResponseDTO>> reopenTicket(
            @PathVariable Long ticketId
    ) {
        SupportTicketResponseDTO ticket = supportService.reopenTicket(ticketId);
        return ResponseEntity.ok(
                ApiResponse.ok("Ticket rouvert avec succès.", ticket)
        );
    }

    // ============================================================
    // FAQ — LISTER
    // ============================================================
    @GetMapping("/faq")
    public ResponseEntity<ApiResponse<Page<FaqEntrySummaryDTO>>> getAllFaqs(
            @RequestParam(required = false) FaqEntry.RoleType roleType,
            @RequestParam(required = false) FaqEntry.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<FaqEntrySummaryDTO> faqs = supportService.getAllFaqs(roleType, status, page, size);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ récupérées avec succès.", faqs)
        );
    }

    // ============================================================
    // FAQ — DÉTAIL
    // ============================================================
    @GetMapping("/faq/{faqId}")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> getFaqDetail(
            @PathVariable Long faqId
    ) {
        FaqEntryResponseDTO faq = supportService.getFaqDetail(faqId);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ récupérée avec succès.", faq)
        );
    }

    // ============================================================
    // FAQ — CRÉER
    // ============================================================
    @PostMapping("/faq")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> createFaq(
            @Valid @RequestBody FaqEntryRequestDTO request
    ) {
        FaqEntryResponseDTO faq = supportService.createFaq(request);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ créée avec succès.", faq)
        );
    }

    // ============================================================
    // FAQ — MODIFIER
    // ============================================================
    @PutMapping("/faq/{faqId}")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> updateFaq(
            @PathVariable Long faqId,
            @Valid @RequestBody FaqEntryUpdateDTO request
    ) {
        FaqEntryResponseDTO faq = supportService.updateFaq(faqId, request);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ mise à jour avec succès.", faq)
        );
    }

    // ============================================================
    // FAQ — ACTIVER
    // ============================================================
    @PutMapping("/faq/{faqId}/activate")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> activateFaq(
            @PathVariable Long faqId
    ) {
        FaqEntryResponseDTO faq = supportService.activateFaq(faqId);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ activée avec succès.", faq)
        );
    }

    // ============================================================
    // FAQ — DÉSACTIVER
    // ============================================================
    @PutMapping("/faq/{faqId}/deactivate")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> deactivateFaq(
            @PathVariable Long faqId
    ) {
        FaqEntryResponseDTO faq = supportService.deactivateFaq(faqId);
        return ResponseEntity.ok(
                ApiResponse.ok("FAQ désactivée avec succès.", faq)
        );
    }

    // ============================================================
    // FAQ — SUPPRIMER
    // ============================================================
    @DeleteMapping("/faq/{faqId}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(
            @PathVariable Long faqId
    ) {
        supportService.deleteFaq(faqId);
        return ResponseEntity.ok(
                ApiResponse.okMessage("FAQ supprimée avec succès.")
        );
    }
}