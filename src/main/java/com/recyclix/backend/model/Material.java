package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "materials")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le nom du matériau ne peut pas être nul.")
    @Size(max = 100, message = "Le nom du matériau ne peut pas dépasser 100 caractères.")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⚠️ Mets optional=false seulement si chaque Material a TOUJOURS un prix dès création
    @OneToOne(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private MaterialPrice materialPrice;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CollectionRequest> collectionRequests = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    // Méthodes métier (optionnel)
    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}