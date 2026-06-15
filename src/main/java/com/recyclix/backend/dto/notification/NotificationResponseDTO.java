package com.recyclix.backend.dto.notification;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;

    private Long accountId;

    private String title;
    private String message;
    private String type;

    private Boolean isRead;
    private String targetRole;
    private LocalDateTime createdAt;
}