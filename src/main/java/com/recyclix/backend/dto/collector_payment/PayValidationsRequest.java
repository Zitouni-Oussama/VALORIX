package com.recyclix.backend.dto.collector_payment;

import com.recyclix.backend.model.Payment;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayValidationsRequest {
    private List<Long> validationIds;
    private Payment.PaymentMethod paymentMethod;
}