package com.recyclix.backend.dto.wallet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "La devise ne peut pas être nulle.")
    @Size(max = 10, message = "La devise ne peut pas dépasser 10 caractères.")
    private String currency;
}