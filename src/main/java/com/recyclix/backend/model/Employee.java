package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employees")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String lastName;

    @Past
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Pattern(regexp = "^(05|06|07)[0-9]{8}$")
    @Column(length = 20)
    private String phone;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "salary_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal salaryAmount;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "wilaya", length = 100)
    private String wilaya;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycling_center_id")
    private RecyclingCenter recyclingCenter;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (active == null) active = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}