package com.hoops.notification.adapter.in.web.dto;

import com.hoops.notification.domain.model.Notification;
import com.hoops.notification.domain.vo.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long relatedMatchId,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedMatchId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
