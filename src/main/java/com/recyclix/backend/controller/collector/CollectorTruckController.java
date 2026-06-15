package com.recyclix.backend.controller.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.truck.TruckRequestDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.dto.truck.TruckUpdateDTO;
import com.recyclix.backend.service.collector.CollectorTruckService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/collector/truck")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorTruckController {

    private final CollectorTruckService collectorTruckService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> getMyTruck() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Camion récupéré avec succès.",
                        collectorTruckService.getMyTruck()
                )
        );
    }

    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TruckResponseDTO>> createMyTruck(
            @RequestPart("truck") String truckJson,
            @RequestPart(value = "truckPhoto", required = false) MultipartFile truckPhoto,
            @RequestPart(value = "greyCardImage", required = false) MultipartFile greyCardImage,
            @RequestPart(value = "drivingLicenseImage", required = false) MultipartFile drivingLicenseImage
    ) throws Exception {
        TruckRequestDTO dto = objectMapper.readValue(truckJson, TruckRequestDTO.class);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Camion créé avec succès.",
                        collectorTruckService.createMyTruck(dto, truckPhoto, greyCardImage, drivingLicenseImage)
                )
        );
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TruckResponseDTO>> updateMyTruck(
            @RequestPart(value = "truck", required = false) String truckJson,
            @RequestPart(value = "truckPhoto", required = false) MultipartFile truckPhoto,
            @RequestPart(value = "greyCardImage", required = false) MultipartFile greyCardImage,
            @RequestPart(value = "drivingLicenseImage", required = false) MultipartFile drivingLicenseImage
    ) throws Exception {
        TruckUpdateDTO dto = null;

        if (truckJson != null && !truckJson.isBlank()) {
            dto = objectMapper.readValue(truckJson, TruckUpdateDTO.class);
        }

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Camion mis à jour avec succès.",
                        collectorTruckService.updateMyTruck(dto, truckPhoto, greyCardImage, drivingLicenseImage)
                )
        );
    }

    @PutMapping("/me/activate")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> activateMyTruck() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Camion activé avec succès.",
                        collectorTruckService.activateMyTruck()
                )
        );
    }

    @PutMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> deactivateMyTruck() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Camion désactivé avec succès.",
                        collectorTruckService.deactivateMyTruck()
                )
        );
    }

    @DeleteMapping("/me/truck-photo")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> removeMyTruckPhoto() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Photo du camion supprimée avec succès.",
                        collectorTruckService.removeMyTruckPhoto()
                )
        );
    }

    @DeleteMapping("/me/grey-card")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> removeMyGreyCardImage() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Image de la carte grise supprimée avec succès.",
                        collectorTruckService.removeMyGreyCardImage()
                )
        );
    }

    @DeleteMapping("/me/driving-license")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> removeMyDrivingLicenseImage() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Image du permis supprimée avec succès.",
                        collectorTruckService.removeMyDrivingLicenseImage()
                )
        );
    }
}