package com.recyclix.backend.dto.transaction;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateDTO {

    // Généralement vide (transaction immuable)
    private String description;
}