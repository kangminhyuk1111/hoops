package com.hoops.notification.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Notification {

    private final Long id;
    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long relatedMatchId;
    private Boolean isRead;
    private final LocalDateTime createdAt;

    public Notification(Long id, Long userId, NotificationType type, String title, String message,
            Long relatedMatchId, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedMatchId = relatedMatchId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
