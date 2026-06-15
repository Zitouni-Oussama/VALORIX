// controller/accountant/WithdrawalManagementController.java
package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.dto.accountant.WithdrawalApprovalRequest;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.TransactionMapper;
import com.recyclix.backend.model.Transaction;
import com.recyclix.backend.repository.TransactionRepository;
import com.recyclix.backend.service.accountant.WithdrawalApprovalService;
import com.recyclix.backend.util.ApiResponse;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accountant/withdrawals")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class WithdrawalManagementController {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WithdrawalApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TransactionResponseDTO>>> getPendingWithdrawals() {
        List<Transaction> pending = transactionRepository.findByTypeAndStatus(
                Transaction.TransactionType.CITIZEN_PAYMENT,
                Transaction.TransactionStatus.PENDING
        );
        List<TransactionResponseDTO> dtos = pending.stream()
                .map(transactionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionResponseDTO>>> getWithdrawalHistory() {
        List<Transaction> completedOrCancelled = transactionRepository.findByTypeAndStatus(
                Transaction.TransactionType.CITIZEN_PAYMENT,
                Transaction.TransactionStatus.COMPLETED
        );
        completedOrCancelled.addAll(
                transactionRepository.findByTypeAndStatus(
                        Transaction.TransactionType.CITIZEN_PAYMENT,
                        Transaction.TransactionStatus.CANCELLED
                )
        );
        List<TransactionResponseDTO> dtos = completedOrCancelled.stream()
                .map(transactionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @PutMapping("/{transactionId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveWithdrawal(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, String> body) {
        Long currentAccountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Non authentifié"));
        String note = body != null ? body.get("note") : null;
        approvalService.approveWithdrawal(transactionId, note, currentAccountId);
        return ResponseEntity.ok(ApiResponse.okMessage("Retrait approuvé avec succès"));
    }

    @PutMapping("/{transactionId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectWithdrawal(
            @PathVariable Long transactionId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Le motif du rejet est obligatoire");
        }
        approvalService.rejectWithdrawal(transactionId, reason);
        return ResponseEntity.ok(ApiResponse.okMessage("Retrait rejeté"));
    }
}