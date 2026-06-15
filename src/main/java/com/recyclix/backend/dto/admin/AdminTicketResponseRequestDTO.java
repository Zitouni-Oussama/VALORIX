// recyclix\backend\dto\admin\AdminTicketResponseRequestDTO.java
package com.recyclix.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTicketResponseRequestDTO {

    @NotBlank(message = "La réponse est obligatoire.")
    private String responseMessage;
}