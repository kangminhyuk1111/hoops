package com.hoops.notification.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 알림을 찾을 수 없을 때 발생하는 예외
 */
public class NotificationNotFoundException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "NOTIFICATION_NOT_FOUND";

    public NotificationNotFoundException(Long notificationId) {
        super(DEFAULT_ERROR_CODE,
                String.format("알림을 찾을 수 없습니다. (알림 ID: %d)", notificationId));
    }
}
