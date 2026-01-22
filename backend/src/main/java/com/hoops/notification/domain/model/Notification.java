package com.hoops.notification.domain.model;

import com.hoops.notification.domain.vo.NotificationType;
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

    private Notification(Long id, Long userId, NotificationType type, String title,
                         String message, Long relatedMatchId, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedMatchId = relatedMatchId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    /**
     * 새로운 알림을 생성한다.
     * 도메인 불변식을 검증한다.
     */
    public static Notification createNew(Long userId, NotificationType type, String title,
                                          String message, Long relatedMatchId) {
        validateRequired(userId, "userId");
        validateRequired(type, "type");
        validateRequired(title, "title");
        return new Notification(null, userId, type, title, message, relatedMatchId,
                false, LocalDateTime.now());
    }

    /**
     * 데이터베이스에서 복원할 때 사용한다.
     * 이미 검증된 데이터이므로 검증을 생략한다.
     */
    public static Notification reconstitute(Long id, Long userId, NotificationType type,
                                             String title, String message, Long relatedMatchId,
                                             Boolean isRead, LocalDateTime createdAt) {
        return new Notification(id, userId, type, title, message, relatedMatchId, isRead, createdAt);
    }

    /**
     * 알림을 읽음 상태로 변경한다.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    private static void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
