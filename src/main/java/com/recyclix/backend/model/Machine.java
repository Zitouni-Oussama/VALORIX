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
@Table(name = "machines")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le nom de la machine ne peut pas être nul.")
    @Size(max = 100, message = "Le nom de la machine ne peut pas dépasser 100 caractères.")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Le numéro de série ne peut pas être nul.")
    @Size(max = 100, message = "Le numéro de série ne peut pas dépasser 100 caractères.")
    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @NotNull(message = "Le statut de la machine ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MachineStatus status;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<MachineIncident> incidents = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = MachineStatus.WORKING;
    }

    public enum MachineStatus {
        WORKING,
        MAINTENANCE,
        OUT_OF_SERVICE
    }
}