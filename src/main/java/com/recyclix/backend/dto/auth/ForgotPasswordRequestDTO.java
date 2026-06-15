package com.recyclix.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequestDTO {

    @Email(message = "L'email doit être valide.")
    @Size(max = 255)
    private String email;

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;
}