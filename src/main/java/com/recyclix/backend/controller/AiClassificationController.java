package com.recyclix.backend.controller;

import com.recyclix.backend.dto.ai_classification.ClassificationResponseDTO;
import com.recyclix.backend.service.HuggingFaceClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiClassificationController {

    private final HuggingFaceClassificationService service;

    @PostMapping(
            value = "/classify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ClassificationResponseDTO> classify(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        return ResponseEntity.ok(
                service.classify(file)
        );
    }
}