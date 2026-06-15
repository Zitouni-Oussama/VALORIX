package com.recyclix.backend.dto.auth;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;

    private AccountResponseDTO account;

    private Long clientId;
    private Long collectorId;
    private Long factoryUserId;
    private Long walletId;
}