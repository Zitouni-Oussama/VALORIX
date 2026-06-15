// recyclix\backend\dto\faq_entry\FaqEntryUpdateDTO.java
package com.recyclix.backend.dto.faq_entry;

import com.recyclix.backend.model.FaqEntry.RoleType;
import com.recyclix.backend.model.FaqEntry.Status;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqEntryUpdateDTO {

    @Size(max = 500, message = "La question ne peut pas dépasser 500 caractères.")
    private String question;

    @Size(max = 2000, message = "La réponse ne peut pas dépasser 2000 caractères.")
    private String answer;

    // ✅ RÔLE CIBLE (modifiable)
    private RoleType roleType;

    // ✅ CATÉGORIE (clé interne)
    @Size(min = 2, max = 80, message = "La catégorie doit être entre 2 et 80 caractères.")
    private String categoryKey;

    // ✅ LABEL DE CATÉGORIE (affiché)
    @Size(min = 2, max = 120, message = "Le label doit être entre 2 et 120 caractères.")
    private String categoryLabel;

    // ✅ ORDRE D'AFFICHAGE
    private Integer displayOrder;

    // ✅ STATUT
    private Status status;

    private Long createdById;
}