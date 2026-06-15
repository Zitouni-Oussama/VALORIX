package com.recyclix.backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// java/com/recyclix/backend/dto/payment/PaymentResponseDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long accountId;
    private String clientEmail;   // ← NOUVEAU
    private BigDecimal amount;
    private String method;
    private String status;
    private String referenceNumber;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private String description;

    private String accountHolderName;
    private String iban;
    private String bankName;
}