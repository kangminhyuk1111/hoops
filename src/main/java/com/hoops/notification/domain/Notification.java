package com.hoops.notification.domain;

import java.time.LocalDateTime;

public class Notification {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedMatchId;
    private Boolean isRead;
    private LocalDateTime createdAt;

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

    // Domain Logic - Mark as read
    public void markAsRead() {
        this.isRead = true;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getRelatedMatchId() {
        return relatedMatchId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
