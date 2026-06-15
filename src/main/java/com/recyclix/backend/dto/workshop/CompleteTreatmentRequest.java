package com.recyclix.backend.dto.workshop;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CompleteTreatmentRequest {
    @NotNull
    @PositiveOrZero
    private BigDecimal processedQuantityKg;

    private String notes;

    public BigDecimal getProcessedQuantityKg() { return processedQuantityKg; }
    public void setProcessedQuantityKg(BigDecimal processedQuantityKg) { this.processedQuantityKg = processedQuantityKg; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}