package com.recyclix.backend.dto.factory_user;

import com.recyclix.backend.model.FactoryUser;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryUserResponseDTO {

    private Long id;

    private Long accountId;

    private String firstName;
    private String lastName;
    private String employeeNumber;
    private FactoryUser.FactoryPosition position;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // counts (au lieu d'envoyer les listes)
    private Integer validationsCount;
    private Integer expensesCount;
    private Integer financialReportsCount;
    private Integer recordedAbsencesCount;
    private Integer reportedIncidentsCount;
    private Integer aiClassificationsCount;
    private Integer faqEntriesCount;
    private Integer supportTicketsCount;

    private Boolean isHeadAccountant;
}