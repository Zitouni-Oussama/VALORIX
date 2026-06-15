// recyclix\backend\dto\admin\AdminTicketAssignRequestDTO.java
package com.recyclix.backend.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTicketAssignRequestDTO {

    @NotNull(message = "L'identifiant du FactoryUser est obligatoire.")
    private Long assignedToId;
}