package com.hoops.notification.infrastructure.mapper;

import com.hoops.notification.domain.Notification;
import com.hoops.notification.infrastructure.NotificationEntity;

public class NotificationMapper {

    public static Notification toDomain(NotificationEntity entity) {
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

    public static NotificationEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }
        if (domain.getId() != null) {
            return new NotificationEntity(
                    domain.getId(),
                    domain.getUserId(),
                    domain.getType(),
                    domain.getTitle(),
                    domain.getMessage(),
                    domain.getRelatedMatchId(),
                    domain.getIsRead(),
                    domain.getCreatedAt());
        }
        return new NotificationEntity(
                domain.getUserId(),
                domain.getType(),
                domain.getTitle(),
                domain.getMessage(),
                domain.getRelatedMatchId(),
                domain.getIsRead(),
                domain.getCreatedAt());
    }
}
