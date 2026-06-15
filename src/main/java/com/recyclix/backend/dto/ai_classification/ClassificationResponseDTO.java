package com.recyclix.backend.dto.ai_classification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationResponseDTO {

    private String predictedClass;

    private Double confidence;

    private Map<String, Double> probabilities;
}