// recyclix\backend\dto\faq_entry\FaqEntryResponseDTO.java
package com.recyclix.backend.dto.faq_entry;

import com.recyclix.backend.model.FaqEntry.RoleType;
import com.recyclix.backend.model.FaqEntry.Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqEntryResponseDTO {

    private Long id;

    private String question;
    private String answer;

    // ✅ AJOUTER CES CHAMPS
    private RoleType roleType;
    private String categoryKey;
    private String categoryLabel;
    private Integer displayOrder;
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long createdById;
}