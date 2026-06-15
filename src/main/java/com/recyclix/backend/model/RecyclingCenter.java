package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recycling_centers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecyclingCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le nom du centre de recyclage ne peut pas être nul.")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull(message = "La localisation du centre ne peut pas être nulle.")
    @Size(max = 255)
    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @NotNull(message = "La capacité du centre ne peut pas être nulle.")
    @Column(name = "capacity", nullable = false, precision = 19, scale = 3)
    private BigDecimal capacity;

    @Size(max = 255)
    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recyclingCenter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FactoryDelivery> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "recyclingCenter")
    private List<FactoryUser> factoryUsers = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}