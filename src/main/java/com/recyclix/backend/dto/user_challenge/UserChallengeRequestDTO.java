package com.recyclix.backend.dto.user_challenge;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le challengeId ne peut pas être nul.")
    private Long challengeId;
}