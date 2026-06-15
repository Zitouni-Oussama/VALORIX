package com.recyclix.backend.dto.wallet;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletUpdateDTO {

    @Size(max = 10)
    private String currency;

    // ⚠️ balance non modifiable directement (via transactions uniquement)
}