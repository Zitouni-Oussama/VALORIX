package com.recyclix.backend.dto.account;

import com.recyclix.backend.model.Account.AccountStatus;
import com.recyclix.backend.model.Account.RoleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestDTO {

    @Email(message = "L'email doit être valide.")
    @NotNull(message = "L'email ne peut pas être nul.")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères.")
    private String email;

    @Pattern(
            regexp = "^(05|06|07)[0-9]{8}$",
            message = "Le téléphone doit commencer par 05, 06 ou 07 et contenir 10 chiffres."
    )
    private String phone;

    @NotNull(message = "Le mot de passe ne peut pas être nul.")
    @Size(min = 8, message = "Le mot de passe doit comporter au moins 8 caractères.")
    private String password;

    @NotNull(message = "Le rôle ne peut pas être nul.")
    private RoleType roleType;

    private String profileImageUrl;

    // Optionnel: si null -> @PrePersist met ACTIVE
    private AccountStatus status;
}