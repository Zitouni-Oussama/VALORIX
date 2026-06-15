package com.recyclix.backend.dto.notification;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryDTO {

    private Long id;

    private String title;
    private String type;

    private Boolean isRead;

    private LocalDateTime createdAt;
}