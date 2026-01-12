package com.hoops.notification.application.port.in;

public interface GetUnreadCountUseCase {

    int getUnreadCount(Long userId);
}
