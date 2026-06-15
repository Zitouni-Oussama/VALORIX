package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
    @NotNull(message = "Le montant ne peut pas être nul.")
    @Column(name = "amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal amount;

    @NotNull(message = "La méthode de paiement ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "description")
    private String description;

    @Setter(AccessLevel.NONE)
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @NotNull(message = "Le statut du paiement ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : un paiement est lié à un seul compte
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Account account;

    // dans com.recyclix.backend.model.Payment

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "iban", nullable = false, length = 50)
    private String iban;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;


    @PrePersist
    void onCreate() {
        if (this.paymentDate == null) this.paymentDate = LocalDateTime.now();
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }

    // ✅ Méthodes métier propres
    public void complete() {
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }



    // Enum pour les méthodes de paiement
    public enum PaymentMethod {
        CASH,
        CARD,
        BANK_TRANSFER,
        ONLINE
    }

    // Enum pour le statut du paiement
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}