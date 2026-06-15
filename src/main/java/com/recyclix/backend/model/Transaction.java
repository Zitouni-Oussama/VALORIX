package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le type de transaction ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @NotNull(message = "Le montant de la transaction ne peut pas être nul.")
    @Column(name = "amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal amount;

    @NotNull(message = "Le statut de la transaction ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_collection_id", unique = true)
    @JsonIgnore
    private Collection collection;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "iban", nullable = false, length = 50)
    private String iban;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "Description")
    private String Description;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = TransactionStatus.PENDING;
    }

    // ✅ Méthodes métier propres
    public void complete() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void cancel() {
        this.status = TransactionStatus.CANCELLED;
    }

    // Enum pour les types de transaction
    public enum TransactionType {
        CITIZEN_PAYMENT,
        FACTORY_SALE,
        POINT_CONVERSION,
        EXPENSE
    }

    // Enum pour les statuts de la transaction
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        CANCELLED
    }
}