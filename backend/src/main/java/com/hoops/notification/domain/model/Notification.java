package com.hoops.notification.domain.model;

import com.hoops.notification.domain.vo.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Notification {

    private final Long id;
    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long relatedMatchId;
    private Boolean isRead;
    private final LocalDateTime createdAt;

    public void markAsRead() {
        this.isRead = true;
    }
}
