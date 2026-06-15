package com.recyclix.backend.dto.payment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentUpdateDTO {

    private String status;
    private LocalDateTime paidAt;

    private String description;
}