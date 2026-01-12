package com.hoops.notification.adapter.in.web;

import com.hoops.notification.adapter.in.web.dto.NotificationResponse;
import com.hoops.notification.adapter.in.web.dto.UnreadCountResponse;
import com.hoops.notification.application.port.in.GetNotificationsUseCase;
import com.hoops.notification.application.port.in.GetUnreadCountUseCase;
import com.hoops.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.hoops.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final GetUnreadCountUseCase getUnreadCountUseCase;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Long userId) {
        List<Notification> notifications = getNotificationsUseCase.getNotifications(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId) {
        markNotificationAsReadUseCase.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        int count = getUnreadCountUseCase.getUnreadCount(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
}
