package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.service.accountant.PaymentTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accountant/tracking")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class PaymentTrackingController {

    private final PaymentTrackingService trackingService;

    // 1. Obtenir l'argent total attendu (Pour le Dashboard)
    @GetMapping("/expected-revenue")
    public ResponseEntity<BigDecimal> getExpectedRevenue() {
        return ResponseEntity.ok(trackingService.getTotalExpectedRevenue());
    }

    // 2. Obtenir la liste urgente des retards de paiement (Pour les alertes du comptable)
    @GetMapping("/urgent-overdue")
    public ResponseEntity<List<FactoryInvoice>> getUrgentOverdueInvoices() {
        return ResponseEntity.ok(trackingService.getUrgentOverdueInvoices());
    }

    // 3. Forcer la vérification manuellement (Très utile pour vous quand vous testez l'application !)
    @PostMapping("/trigger-check")
    public ResponseEntity<String> triggerManualCheck() {
        trackingService.autoCheckOverdueInvoices();
        return ResponseEntity.ok("Vérification des retards effectuée avec succès.");
    }
}