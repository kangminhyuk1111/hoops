package com.hoops.notification.adapter.out.persistence;

import com.hoops.notification.domain.model.Notification;

public class NotificationMapper {

    private NotificationMapper() {
    }

    public static Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Notification(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getRelatedMatchId(),
                entity.getIsRead(),
                entity.getCreatedAt());
    }

    public static NotificationJpaEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }
        if (domain.getId() != null) {
            return new NotificationJpaEntity(
                    domain.getId(),
                    domain.getUserId(),
                    domain.getType(),
                    domain.getTitle(),
                    domain.getMessage(),
                    domain.getRelatedMatchId(),
                    domain.getIsRead(),
                    domain.getCreatedAt());
        }
        return new NotificationJpaEntity(
                domain.getUserId(),
                domain.getType(),
                domain.getTitle(),
                domain.getMessage(),
                domain.getRelatedMatchId(),
                domain.getIsRead(),
                domain.getCreatedAt());
    }
}
