package com.hoops.notification.domain.repository;

import com.hoops.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByUserId(Long userId);

    int countUnreadByUserId(Long userId);
}
