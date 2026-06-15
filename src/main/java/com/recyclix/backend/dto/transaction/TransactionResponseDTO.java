package com.recyclix.backend.dto.transaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private Long id;

    private Long accountId;
    private Long collectionId;
    private Long paymentId;

    private String type;
    private BigDecimal amount;
    private String description;

    private String status;

    private LocalDateTime createdAt;

    // dto/transaction/TransactionResponseDTO.java (ajouts)
    private String accountHolderName;
    private String iban;
    private String bankName;

    private String accountEmail;
}