// dto/accountant/WithdrawalApprovalRequest.java
package com.recyclix.backend.dto.accountant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawalApprovalRequest {
    @NotNull
    private Long transactionId;
    private String note; // optionnel
}