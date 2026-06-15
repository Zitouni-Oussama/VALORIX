package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.dto.point_movement.PointMovementResponseDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.dto.wallet.WalletResponseDTO;
import com.recyclix.backend.service.client.ClientWalletService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientWalletController {

    private final ClientWalletService clientWalletService;

    //. -------------------- GET MY WALLET OVERVIEW -------------------- .\\
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<ClientWalletService.ClientWalletOverviewResponse>> getMyWalletOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Vue globale du wallet récupérée avec succès.",
                        clientWalletService.getMyWalletOverview()
                )
        );
    }

    //. -------------------- GET MY WALLET ONLY -------------------- .\\
    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getMyWallet() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Wallet récupéré avec succès.",
                        clientWalletService.getMyWallet()
                )
        );
    }

    //. -------------------- GET MY TRANSACTIONS -------------------- .\\
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponseDTO>>> getMyTransactions() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Transactions récupérées avec succès.",
                        clientWalletService.getMyTransactions()
                )
        );
    }

    //. -------------------- GET MY PAYMENTS -------------------- .\\
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getMyPayments() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Paiements récupérés avec succès.",
                        clientWalletService.getMyPayments()
                )
        );
    }

    //. -------------------- GET MY POINT MOVEMENTS -------------------- .\\
    @GetMapping("/point-movements")
    public ResponseEntity<ApiResponse<List<PointMovementResponseDTO>>> getMyPointMovements() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Mouvements de points récupérés avec succès.",
                        clientWalletService.getMyPointMovements()
                )
        );
    }
}