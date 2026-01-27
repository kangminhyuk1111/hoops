package com.hoops.notification.application.service;

import com.hoops.notification.application.exception.NotificationAccessDeniedException;
import com.hoops.notification.application.exception.NotificationNotFoundException;
import com.hoops.notification.application.port.in.CreateNotificationUseCase;
import com.hoops.notification.application.port.in.GetNotificationsUseCase;
import com.hoops.notification.application.port.in.GetUnreadCountUseCase;
import com.hoops.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.hoops.notification.domain.model.Notification;
import com.hoops.notification.domain.vo.NotificationType;
import com.hoops.notification.application.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService implements
        GetNotificationsUseCase,
        MarkNotificationAsReadUseCase,
        GetUnreadCountUseCase,
        CreateNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepositoryPort notificationRepository;

    @Override
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException(notificationId, userId);
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String title,
                                           String message, Long relatedMatchId) {
        Notification notification = Notification.createNew(userId, type, title, message, relatedMatchId);

        Notification saved = notificationRepository.save(notification);
        log.info("알림 생성 완료: userId={}, type={}, matchId={}", userId, type, relatedMatchId);
        return saved;
    }
}
