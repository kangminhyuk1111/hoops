package com.hoops.notification.domain.repository;

import com.hoops.notification.domain.Notification;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);

    Optional<Notification> findById(Long id);
}
