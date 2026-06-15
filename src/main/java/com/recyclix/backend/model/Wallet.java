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
@Table(name = "wallets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : un wallet ne change jamais de compte
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    @JsonIgnore
    private Account account;

    @NotNull(message = "Le solde d'argent ne peut pas être nul.")
    @Column(name = "balance_money", nullable = false, precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal balanceMoney = BigDecimal.ZERO;

    @NotNull(message = "Le solde de points ne peut pas être nul.")
    @Column(name = "balance_points", nullable = false)
    @Builder.Default
    private Integer balancePoints = 0;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        if (this.balanceMoney == null) this.balanceMoney = BigDecimal.ZERO;
        if (this.balancePoints == null) this.balancePoints = 0;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ Méthodes métier sûres
    public void addMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("amount doit être > 0");
        this.balanceMoney = this.balanceMoney.add(amount);
    }

    public void subtractMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("amount doit être > 0");
        if (this.balanceMoney.compareTo(amount) < 0)
            throw new IllegalStateException("Solde insuffisant");
        this.balanceMoney = this.balanceMoney.subtract(amount);
    }

    public void addPoints(int points) {
        if (points <= 0) throw new IllegalArgumentException("points doit être > 0");
        this.balancePoints += points;
    }

    public void subtractPoints(int points) {
        if (points <= 0) throw new IllegalArgumentException("points doit être > 0");
        if (this.balancePoints < points)
            throw new IllegalStateException("Points insuffisants");
        this.balancePoints -= points;
    }


}