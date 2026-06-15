package com.recyclix.backend.dto.workshop;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class StartTreatmentRequest {
    @NotNull
    private Long materialId;

    @NotNull
    @Positive
    private BigDecimal quantityKg;

    private String notes;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getQuantityKg() { return quantityKg; }
    public void setQuantityKg(BigDecimal quantityKg) { this.quantityKg = quantityKg; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}