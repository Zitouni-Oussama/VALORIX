package com.recyclix.backend.dto.notification;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUpdateDTO {

    private Boolean isRead;
}