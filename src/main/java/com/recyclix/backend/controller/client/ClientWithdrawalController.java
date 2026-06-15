// controller/client/ClientWithdrawalController.java
package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.transaction.WithdrawalRequestDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.service.client.ClientWithdrawalService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientWithdrawalController {

    private final ClientWithdrawalService clientWithdrawalService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO request) {
        TransactionResponseDTO transaction = clientWithdrawalService.requestWithdrawal(request);
        return ResponseEntity.ok(ApiResponse.ok("Demande de retrait enregistrée avec succès", transaction));
    }
}