package com.recyclix.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.ai_classification.ClassificationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HuggingFaceClassificationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${huggingface.base-url}")
    private String baseUrl;

    public ClassificationResponseDTO classify(MultipartFile file)
            throws Exception {

        String uploadedPath = uploadImage(file);

        String eventId = startPrediction(uploadedPath);

        return getPredictionResult(eventId);
    }

    /**
     * STEP 1 : Upload image
     */
    private String uploadImage(MultipartFile file)
            throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource resource =
                new ByteArrayResource(file.getBytes()) {

                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("files", resource);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String[]> response =
                restTemplate.postForEntity(
                        baseUrl + "/gradio_api/upload",
                        request,
                        String[].class
                );

        if (response.getBody() == null
                || response.getBody().length == 0) {
            throw new RuntimeException(
                    "Upload failed."
            );
        }

        return response.getBody()[0];
    }

    /**
     * STEP 2 : Predict
     */
    private String startPrediction(String uploadedPath) {

        Map<String, Object> imageData =
                new HashMap<>();

        imageData.put("path", uploadedPath);

        imageData.put(
                "meta",
                Map.of("_type", "gradio.FileData")
        );

        Map<String, Object> payload =
                Map.of(
                        "data",
                        List.of(imageData)
                );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        baseUrl +
                                "/gradio_api/call/predict",
                        payload,
                        Map.class
                );

        if (response.getBody() == null
                || !response.getBody()
                .containsKey("event_id")) {

            throw new RuntimeException(
                    "No event_id returned."
            );
        }

        return response.getBody()
                .get("event_id")
                .toString();
    }

    /**
     * STEP 3 : Get result
     */

    private ClassificationResponseDTO getPredictionResult(String eventId) throws Exception {
        String rawResponse = restTemplate.getForObject(
                baseUrl + "/gradio_api/call/predict/" + eventId,
                String.class
        );

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new RuntimeException("Empty response from Gradio");
        }

        // 1. Extraire le JSON (premier [ ou {)
        int start = rawResponse.indexOf('[');
        if (start == -1) start = rawResponse.indexOf('{');
        if (start == -1) {
            throw new RuntimeException("No JSON found in response: " + rawResponse);
        }
        int end = rawResponse.lastIndexOf(']');
        if (end == -1) end = rawResponse.lastIndexOf('}');
        if (end == -1) {
            throw new RuntimeException("No closing bracket in response");
        }
        String jsonLine = rawResponse.substring(start, end + 1);

        // 2. Parser le JSON
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> predictions = mapper.readValue(jsonLine,
                new com.fasterxml.jackson.core.type.TypeReference<>() {});

        if (predictions.isEmpty()) {
            return ClassificationResponseDTO.builder()
                    .predictedClass("UNKNOWN")
                    .confidence(0.0)
                    .probabilities(Collections.emptyMap())
                    .build();
        }

        Map<String, Double> probabilities = new HashMap<>();
        String bestLabel = "";
        double bestConfidence = 0.0;

        Map<String, Object> first = predictions.get(0);
        List<Map<String, Object>> confidences = (List<Map<String, Object>>) first.get("confidences");

        for (Map<String, Object> entry : confidences) {
            String label = (String) entry.get("label");
            double confidence = ((Number) entry.get("confidence")).doubleValue();
            probabilities.put(label, confidence);
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                bestLabel = label;
            }
        }

        // Fallback si pas de "confidences"
        if (bestLabel.isEmpty() && first.containsKey("label")) {
            bestLabel = (String) first.get("label");
            bestConfidence = 1.0;
            probabilities.put(bestLabel, bestConfidence);
        }

        return ClassificationResponseDTO.builder()
                .predictedClass(bestLabel)
                .confidence(bestConfidence)
                .probabilities(probabilities)
                .build();
    }


}