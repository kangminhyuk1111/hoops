package com.hoops.notification.application.port.in;

import com.hoops.notification.domain.model.Notification;

import java.util.List;

public interface GetNotificationsUseCase {

    List<Notification> getNotifications(Long userId);
}
