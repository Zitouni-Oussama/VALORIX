package com.recyclix.backend.dto.workshop;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class UpdateTreatmentBatchRequest {
    @Positive
    private BigDecimal initialQuantityKg;
    private String notes;

    // getters/setters
    public BigDecimal getInitialQuantityKg() { return initialQuantityKg; }
    public void setInitialQuantityKg(BigDecimal initialQuantityKg) { this.initialQuantityKg = initialQuantityKg; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}