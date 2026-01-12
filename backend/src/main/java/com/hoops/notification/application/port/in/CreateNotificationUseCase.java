package com.hoops.notification.application.port.in;

import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.NotificationType;

/**
 * 알림 생성 유스케이스
 */
public interface CreateNotificationUseCase {

    Notification createNotification(Long userId, NotificationType type, String title, String message, Long relatedMatchId);
}
