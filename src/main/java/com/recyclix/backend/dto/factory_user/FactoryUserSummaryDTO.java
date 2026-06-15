package com.recyclix.backend.dto.factory_user;

import com.recyclix.backend.model.FactoryUser;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryUserSummaryDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private FactoryUser.FactoryPosition position;
    private String recyclingCenterName;

    private Boolean isHeadAccountant;
}