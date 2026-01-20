package com.hoops.notification.adapter.in.web;

import com.hoops.notification.adapter.in.web.dto.NotificationResponse;
import com.hoops.notification.adapter.in.web.dto.UnreadCountResponse;
import com.hoops.notification.application.port.in.GetNotificationsUseCase;
import com.hoops.notification.application.port.in.GetUnreadCountUseCase;
import com.hoops.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.hoops.notification.domain.model.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final GetUnreadCountUseCase getUnreadCountUseCase;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        List<Notification> notifications = getNotificationsUseCase.getNotifications(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        markNotificationAsReadUseCase.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "읽지 않은 알림의 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        int count = getUnreadCountUseCase.getUnreadCount(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
}
