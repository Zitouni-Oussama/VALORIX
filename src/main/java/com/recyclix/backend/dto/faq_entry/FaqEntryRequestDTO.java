// recyclix\backend\dto\faq_entry\FaqEntryRequestDTO.java
package com.recyclix.backend.dto.faq_entry;

import com.recyclix.backend.model.FaqEntry.RoleType;
import com.recyclix.backend.model.FaqEntry.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqEntryRequestDTO {

    @NotBlank(message = "La question ne peut pas être nulle.")
    @Size(max = 500, message = "La question ne peut pas dépasser 500 caractères.")
    private String question;

    @NotBlank(message = "La réponse ne peut pas être nulle.")
    @Size(max = 2000, message = "La réponse ne peut pas dépasser 2000 caractères.")
    private String answer;

    // ✅ RÔLE CIBLE (CITIZEN, COLLECTOR, ALL)
    @NotNull(message = "Le rôle cible est obligatoire.")
    private RoleType roleType;

    // ✅ CATÉGORIE (clé interne)
    @NotBlank(message = "La catégorie est obligatoire.")
    @Size(min = 2, max = 80, message = "La catégorie doit être entre 2 et 80 caractères.")
    private String categoryKey;

    // ✅ LABEL DE CATÉGORIE (affiché)
    @NotBlank(message = "Le label de catégorie est obligatoire.")
    @Size(min = 2, max = 120, message = "Le label doit être entre 2 et 120 caractères.")
    private String categoryLabel;

    // ✅ ORDRE D'AFFICHAGE
    private Integer displayOrder;

    // ✅ STATUT
    private Status status;

    // Optionnel
    private Long createdById;
}