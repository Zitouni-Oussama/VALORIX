package com.recyclix.backend.dto.factory_user;

import com.recyclix.backend.model.FactoryUser;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryUserUpdateDTO {

    // ⚠️ accountId NON modifiable (OneToOne + Setter NONE)

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @Size(max = 50, message = "Le numéro d'employé ne peut pas dépasser 50 caractères.")
    private String employeeNumber;

    private Boolean isHeadAccountant;

    @Size(max = 100, message = "La position ne peut pas dépasser 100 caractères.")
    private FactoryUser.FactoryPosition position;
}