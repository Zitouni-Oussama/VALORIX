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
@Table(name = "factory_users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : ne pas changer le compte après création
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    @JsonIgnore
    private Account account;

    @NotNull(message = "Le prénom ne peut pas être nul.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "Le numéro d'employé ne peut pas être nul.")
    @Size(max = 50, message = "Le numéro d'employé ne peut pas dépasser 50 caractères.")
    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @NotNull(message = "La position ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 100)
    private FactoryPosition position;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "validatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<FactoryValidation> validations = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "generatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<FinancialReport> financialReports = new ArrayList<>();

    @OneToMany(mappedBy = "recordedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<WorkerAbsence> recordedAbsences = new ArrayList<>();

    @OneToMany(mappedBy = "reportedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<MachineIncident> reportedIncidents = new ArrayList<>();

    @OneToMany(mappedBy = "validatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<AIClassification> aiClassifications = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<FaqEntry> faqEntries = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<SupportTicket> supportTickets = new ArrayList<>();

    // FactoryUser.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycling_center_id")
    private RecyclingCenter recyclingCenter;

    // Ajouter cet attribut avec les autres
    @Column(name = "is_head_accountant")
    private Boolean isHeadAccountant = false;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum FactoryPosition {
        ACCOUNTANT,
        MANAGER,
        ADMIN,
        HR_MANAGER;
    }
}