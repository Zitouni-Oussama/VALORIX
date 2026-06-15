package com.recyclix.backend.dto.account;

import com.recyclix.backend.model.Account.AccountStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdateDTO {

    @Pattern(
            regexp = "^(05|06|07)[0-9]{8}$",
            message = "Le téléphone doit commencer par 05, 06 ou 07 et contenir 10 chiffres."
    )
    private String phone;

    // tu as demandé de garder passwordHash tel quel après traitement
    @Size(min = 8, message = "Le mot de passe doit comporter au moins 8 caractères.")
    private String passwordHash;

    private String profileImageUrl;

    private AccountStatus status;
}