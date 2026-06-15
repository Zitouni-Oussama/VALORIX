package com.recyclix.backend.dto.notification;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le titre ne peut pas être nul.")
    @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères.")
    private String title;

    @NotNull(message = "Le message ne peut pas être nul.")
    @Size(max = 1000, message = "Le message ne peut pas dépasser 1000 caractères.")
    private String message;

    @NotNull(message = "Le type ne peut pas être nul.")
    private String type;
}