package com.recyclix.backend.controller.collector;

import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.dto.wallet.WalletResponseDTO;
import com.recyclix.backend.service.collector.CollectorWalletService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collector/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorWalletController {

    private final CollectorWalletService collectorWalletService;

    //. -------------------- GET MY WALLET OVERVIEW -------------------- .\\
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<CollectorWalletService.CollectorWalletOverviewResponse>> getMyWalletOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Vue globale du wallet collecteur récupérée avec succès.",
                        collectorWalletService.getMyWalletOverview()
                )
        );
    }

    //. -------------------- GET MY WALLET ONLY -------------------- .\\
    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getMyWallet() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Wallet collecteur récupéré avec succès.",
                        collectorWalletService.getMyWallet()
                )
        );
    }

    //. -------------------- GET MY TRANSACTIONS -------------------- .\\
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponseDTO>>> getMyTransactions() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Transactions du collecteur récupérées avec succès.",
                        collectorWalletService.getMyTransactions()
                )
        );
    }

    //. -------------------- GET MY PAYMENTS -------------------- .\\
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getMyPayments() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Paiements du collecteur récupérés avec succès.",
                        collectorWalletService.getMyPayments()
                )
        );
    }
}