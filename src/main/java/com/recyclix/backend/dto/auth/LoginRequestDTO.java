package com.recyclix.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email doit être valide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    private String password;
}