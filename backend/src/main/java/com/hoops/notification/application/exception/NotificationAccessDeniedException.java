package com.hoops.notification.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 알림에 대한 접근 권한이 없을 때 발생하는 예외
 */
public class NotificationAccessDeniedException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "NOTIFICATION_ACCESS_DENIED";

    public NotificationAccessDeniedException(Long notificationId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("해당 알림에 대한 권한이 없습니다. (알림 ID: %d, 사용자 ID: %d)", notificationId, userId));
    }
}
