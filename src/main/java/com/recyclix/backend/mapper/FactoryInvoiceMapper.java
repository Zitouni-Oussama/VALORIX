package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceRequestDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceResponseDTO;
import com.recyclix.backend.model.FactoryInvoice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FactoryInvoiceMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "referenceNumber", ignore = true),
            @Mapping(target = "tvaAmount", ignore = true),
            @Mapping(target = "amountTtc", ignore = true),
            @Mapping(target = "paidDate", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true)
    })
    FactoryInvoice toEntity(FactoryInvoiceRequestDTO dto);

    @Mappings({
            @Mapping(target = "statusLabel", expression = "java(getStatusLabel(entity.getStatus()))"),
            @Mapping(target = "statusColor", expression = "java(getStatusColor(entity.getStatus()))")
    })
    FactoryInvoiceResponseDTO toResponseDTO(FactoryInvoice entity);

    default String getStatusLabel(FactoryInvoice.InvoiceStatus status) {
        if (status == null) return "BROUILLON";
        return switch (status) {
            case DRAFT -> "BROUILLON";
            case PENDING -> "EN ATTENTE";
            case PAID -> "PAYÉE";
            case OVERDUE -> "EN RETARD";
            case CANCELLED -> "ANNULÉE";
            case REFUNDED -> "REMBOURSÉE";
        };
    }

    default String getStatusColor(FactoryInvoice.InvoiceStatus status) {
        if (status == null) return "gray";
        return switch (status) {
            case DRAFT -> "orange";
            case PENDING -> "blue";
            case PAID -> "green";
            case OVERDUE -> "red";
            case CANCELLED -> "darkgray";
            case REFUNDED -> "purple";
        };
    }
}