package com.hoops.notification.application.port.in;

import com.hoops.notification.domain.Notification;

import java.util.List;

public interface GetNotificationsUseCase {

    List<Notification> getNotifications(Long userId);
}
