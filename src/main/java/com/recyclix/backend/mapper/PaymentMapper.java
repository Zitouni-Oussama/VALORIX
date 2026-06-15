package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.payment.PaymentRequestDTO;
import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.dto.payment.PaymentSummaryDTO;
import com.recyclix.backend.dto.payment.PaymentUpdateDTO;
import com.recyclix.backend.model.Payment;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaymentMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    // java/com/recyclix/backend/mapper/PaymentMapper.java
    @Mappings({
            @Mapping(target = "accountId", expression = "java(payment.getAccount() != null ? payment.getAccount().getId() : null)"),
            @Mapping(target = "clientEmail", expression = "java(payment.getAccount() != null ? payment.getAccount().getEmail() : null)"), // ← NOUVEAU
            @Mapping(target = "method", expression = "java(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)"),
            @Mapping(target = "status", expression = "java(payment.getStatus() != null ? payment.getStatus().name() : null)"),
            @Mapping(target = "createdAt", source = "paymentDate")
    })
    PaymentResponseDTO toResponseDTO(Payment payment);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "status", expression = "java(payment.getStatus() != null ? payment.getStatus().name() : null)"),
            @Mapping(target = "createdAt", source = "paymentDate")
    })
    PaymentSummaryDTO toSummaryDTO(Payment payment);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true), // à injecter dans le service via AccountRepository
            @Mapping(target = "paymentDate", ignore = true), // géré par @PrePersist
            @Mapping(target = "paymentMethod", expression = "java(mapPaymentMethod(dto.getMethod()))"),
            @Mapping(target = "status", expression = "java(mapPaymentStatus(dto.getStatus()))"),
            @Mapping(target = "accountHolderName", source = "accountHolderName"),
            @Mapping(target = "iban", source = "iban"),
            @Mapping(target = "bankName", source = "bankName")
    })
    Payment toEntity(PaymentRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "amount", ignore = true),
            @Mapping(target = "paymentMethod", ignore = true),
            @Mapping(target = "paymentDate", ignore = true), // paidAt n'existe pas dans l'entité
            @Mapping(target = "status", expression = "java(dto.getStatus() != null ? mapPaymentStatus(dto.getStatus()) : entity.getStatus())")
    })
    void updateEntityFromDTO(PaymentUpdateDTO dto, @MappingTarget Payment entity);

    // =========================
    // Helpers
    // =========================
    default Payment.PaymentMethod mapPaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Payment.PaymentMethod.valueOf(value.trim().toUpperCase());
    }

    default Payment.PaymentStatus mapPaymentStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Payment.PaymentStatus.valueOf(value.trim().toUpperCase());
    }
}