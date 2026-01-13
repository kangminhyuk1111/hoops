package com.hoops.notification.application.port.in;

public interface MarkNotificationAsReadUseCase {

    void markAsRead(Long notificationId, Long userId);
}
