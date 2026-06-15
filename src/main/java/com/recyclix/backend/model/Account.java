package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    //? ID + les variables :
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @Email(message = "L'email doit être valide.")
    @NotNull(message = "L'email ne peut pas être nul.")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères.")
    @Setter(AccessLevel.NONE)
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^(05|06|07)[0-9]{8}$", message = "Le téléphone doit commencer par 05, 06 ou 07 et contenir 10 chiffres.")
    @Column(name = "phone")
    private String phone;

    @NotNull(message = "Le mot de passe ne peut pas être nul.")
    @Size(min = 8, message = "Le mot de passe doit comporter au moins 8 caractères.")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotNull(message = "Le rôle ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    @Column(name = "role_type", nullable = false, length = 30)
    private RoleType roleType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @NotNull(message = "Le statut ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    //*-------------------------------------------------------------
    //? les relations :
    //! --> Client
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Client client;
    //! --> Collector
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collector collector;
    //! --> Factory user
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private FactoryUser factoryUser;
    //! --> Wallet
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Wallet wallet;
    //! --> Transactions
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> transactions = new ArrayList<>();
    //! --> Points movements
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PointMovement> pointMovements = new ArrayList<>();
    //! --> Payments
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Payment> payments = new ArrayList<>();
    //! --> Notification
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();
    //! --> User challenges
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserChallenge> userChallenges = new ArrayList<>();
    //! --> Supprot
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SupportTicket> supportTickets = new ArrayList<>();


    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        DELETED;
    }

    public enum RoleType {
        CLIENT,
        COLLECTOR,
        FACTORY_USER
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = AccountStatus.ACTIVE;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
