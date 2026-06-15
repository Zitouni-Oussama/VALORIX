package com.recyclix.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Le mot de passe actuel est obligatoire.")
    private String currentPassword;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire.")
    @Size(min = 8, message = "Le nouveau mot de passe doit comporter au moins 8 caractères.")
    private String newPassword;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire.")
    private String confirmPassword;
}