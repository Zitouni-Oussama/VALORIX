package com.recyclix.backend.dto.support_ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketRequestDTO {

    private Long accountId;

    @NotNull(message = "Le sujet ne peut pas être nul.")
    @Size(max = 200, message = "Le sujet ne peut pas dépasser 200 caractères.")
    private String subject;

    @NotNull(message = "La description ne peut pas être nulle.")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères.")
    private String description;

    @NotNull(message = "La priorité ne peut pas être nulle.")
    private String priority;
}