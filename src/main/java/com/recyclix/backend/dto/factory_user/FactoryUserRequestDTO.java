package com.recyclix.backend.dto.factory_user;

import com.recyclix.backend.model.FactoryUser;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryUserRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le prénom ne peut pas être nul.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @NotNull(message = "Le numéro d'employé ne peut pas être nul.")
    @Size(max = 50, message = "Le numéro d'employé ne peut pas dépasser 50 caractères.")
    private String employeeNumber;

    @NotNull(message = "La position ne peut pas être nulle.")
    @Size(max = 100, message = "La position ne peut pas dépasser 100 caractères.")
    private FactoryUser.FactoryPosition position;

    private Boolean isHeadAccountant;
}