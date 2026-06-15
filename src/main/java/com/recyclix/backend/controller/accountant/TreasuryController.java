package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.service.accountant.TreasuryAndWithdrawalService;
import com.recyclix.backend.model.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountant/treasury")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
public class TreasuryController {

    private final TreasuryAndWithdrawalService treasuryService;

    public TreasuryController(TreasuryAndWithdrawalService treasuryService) {
        this.treasuryService = treasuryService;
    }

    @GetMapping("/summary")
    public ResponseEntity<TreasuryAndWithdrawalService.TreasurySummary> getSummary() {
        return ResponseEntity.ok(treasuryService.getTreasurySummary());
    }

    @GetMapping("/withdrawals/pending")
    public ResponseEntity<List<Payment>> getPending() {
        return ResponseEntity.ok(treasuryService.getPendingWithdrawals());
    }

    @PostMapping("/withdrawals/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        treasuryService.approveWithdrawal(id);
        return ResponseEntity.ok("Virement approuvé vers CCP/Edahabia avec succès.");
    }

    @PostMapping("/withdrawals/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable Long id, @RequestBody String reason) {
        treasuryService.rejectWithdrawal(id, reason);
        return ResponseEntity.ok("Virement rejeté. Motif : " + reason);
    }
}