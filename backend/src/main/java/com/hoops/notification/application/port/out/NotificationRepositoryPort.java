package com.hoops.notification.application.port.out;

import com.hoops.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Notification 영속성 포트 인터페이스
 */
public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByUserId(Long userId);

    int countUnreadByUserId(Long userId);
}
