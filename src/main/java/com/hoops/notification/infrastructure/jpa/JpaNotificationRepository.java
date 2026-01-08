package com.hoops.notification.infrastructure.jpa;

import com.hoops.notification.infrastructure.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
