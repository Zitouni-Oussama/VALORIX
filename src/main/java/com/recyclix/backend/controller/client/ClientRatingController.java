package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.rating.RatingRequestDTO;
import com.recyclix.backend.service.client.ClientRatingService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/ratings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientRatingController {

    private final ClientRatingService clientRatingService;

    @PostMapping("/collections/{collectionId}")
    public ApiResponse<CollectionResponseDTO> rateCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody RatingRequestDTO request
    ) {
        return ApiResponse.ok(
                "Collection notée avec succès.",
                clientRatingService.rateCollection(collectionId, request)
        );
    }

    @GetMapping("/collections/{collectionId}/rating")
    public ApiResponse<RatingRequestDTO> getRatingForCollection(
            @PathVariable Long collectionId
    ) {
        return ApiResponse.ok(
                "Note récupérée avec succès.",
                clientRatingService.getRatingForCollection(collectionId)
        );
    }
}