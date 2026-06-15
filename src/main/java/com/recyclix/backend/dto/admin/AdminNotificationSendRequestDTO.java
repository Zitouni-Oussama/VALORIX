// recyclix\backend\dto\admin\AdminNotificationSendRequestDTO.java
package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationSendRequestDTO {

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères.")
    private String title;

    @NotBlank(message = "Le message est obligatoire.")
    @Size(max = 1000, message = "Le message ne peut pas dépasser 1000 caractères.")
    private String message;

    @NotNull(message = "Le type de notification est obligatoire.")
    private NotificationType type;

    // Pour l'envoi ciblé par rôle
    private String targetRole;

    // Pour l'envoi à des utilisateurs spécifiques
    private List<Long> targetAccountIds;
}