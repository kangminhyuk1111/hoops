package com.hoops.notification.infrastructure;

import com.hoops.notification.domain.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "related_match_id")
    private Long relatedMatchId;

    @Column(nullable = false)
    private Boolean isRead;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public NotificationEntity(Long userId, NotificationType type, String title, String message,
            Long relatedMatchId) {
        this(userId, type, title, message, relatedMatchId, false, LocalDateTime.now());
    }

    public NotificationEntity(Long userId, NotificationType type, String title, String message,
            Long relatedMatchId, Boolean isRead, LocalDateTime createdAt) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedMatchId = relatedMatchId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}
