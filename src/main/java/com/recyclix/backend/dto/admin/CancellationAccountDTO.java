package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.Account;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancellationAccountDTO {
    private Long accountId;
    private String email;
    private String fullName;
    private Account.RoleType roleType;
    private Account.AccountStatus status;
    private int cancellationsLast7Days;
    private int warningThreshold;
    private int criticalThreshold;
    private boolean isWarning;
    private boolean isCritical;
}