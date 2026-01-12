package com.hoops.notification.application.service;

import com.hoops.notification.application.port.in.GetNotificationsUseCase;
import com.hoops.notification.application.port.in.GetUnreadCountUseCase;
import com.hoops.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService implements
        GetNotificationsUseCase,
        MarkNotificationAsReadUseCase,
        GetUnreadCountUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}
