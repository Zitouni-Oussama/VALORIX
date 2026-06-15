package com.recyclix.backend.controller.collector;

import com.recyclix.backend.dto.faq_entry.FaqEntryResponseDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.service.collector.CollectorFaqService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collector/faq")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorFaqController {

    private final CollectorFaqService collectorFaqService;

    //. -------------------- GET COLLECTOR FAQ -------------------- .\\
    @GetMapping
    public ResponseEntity<ApiResponse<List<FaqEntrySummaryDTO>>> getCollectorFaq() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "FAQ collecteur récupérée avec succès.",
                        collectorFaqService.getCollectorFaq()
                )
        );
    }

    //. -------------------- GET FAQ DETAIL -------------------- .\\
    @GetMapping("/{faqId}")
    public ResponseEntity<ApiResponse<FaqEntryResponseDTO>> getCollectorFaqById(
            @PathVariable Long faqId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la FAQ récupéré avec succès.",
                        collectorFaqService.getCollectorFaqById(faqId)
                )
        );
    }
}