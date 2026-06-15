package com.recyclix.backend.dto.faq_entry;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqEntrySummaryDTO {

    private Long id;

    private String question;
    private String answer;

    private LocalDateTime createdAt;

    private Long createdById;

    private String categoryLabel;
    private String roleType;
}