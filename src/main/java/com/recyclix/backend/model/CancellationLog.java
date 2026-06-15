// model/CancellationLog.java
package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_logs",
        indexes = {
                @Index(name = "idx_cancel_account", columnList = "account_id"),
                @Index(name = "idx_cancel_date", columnList = "cancelled_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @NotNull
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @NotNull
    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_at_time", length = 20)
    private Account.RoleType roleAtTime;

    @Column(name = "reason", length = 500)
    private String reason;

    @PrePersist
    void onCreate() {
        if (cancelledAt == null) cancelledAt = LocalDateTime.now();
    }
}