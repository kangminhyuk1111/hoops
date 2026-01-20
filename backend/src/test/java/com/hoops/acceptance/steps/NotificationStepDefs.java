package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.NotificationType;
import com.hoops.notification.domain.repository.NotificationRepository;
import com.hoops.user.domain.model.User;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationStepDefs {

    private final TestAdapter testAdapter;
    private final NotificationRepository notificationRepository;
    private final SharedTestContext sharedContext;
    private Notification testNotification;

    public NotificationStepDefs(
            TestAdapter testAdapter,
            NotificationRepository notificationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.notificationRepository = notificationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내게 알림이 {int}개 있다")
    public void 내게_알림이_N개_있다(int count) {
        User user = sharedContext.getTestUser();
        for (int i = 0; i < count; i++) {
            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .type(NotificationType.MATCH_UPCOMING)
                    .title("알림 제목 " + i)
                    .message("알림 메시지 " + i)
                    .relatedMatchId(1L)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }
    }

    @먼저("내게 읽지 않은 알림이 있다")
    public void 내게_읽지_않은_알림이_있다() {
        User user = sharedContext.getTestUser();
        Notification notification = Notification.builder()
                .userId(user.getId())
                .type(NotificationType.PARTICIPATION_CREATED)
                .title("참가 알림")
                .message("경기에 참가 신청되었습니다")
                .relatedMatchId(1L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        testNotification = notificationRepository.save(notification);
    }

    @먼저("내게 읽지 않은 알림이 {int}개 있다")
    public void 내게_읽지_않은_알림이_N개_있다(int count) {
        User user = sharedContext.getTestUser();
        for (int i = 0; i < count; i++) {
            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .type(NotificationType.MATCH_UPCOMING)
                    .title("알림 제목 " + i)
                    .message("알림 메시지 " + i)
                    .relatedMatchId(1L)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }
    }

    @만일("알림 목록 조회 API를 호출한다")
    public void 알림_목록_조회_API를_호출한다() {
        String token = sharedContext.getAccessToken();
        TestResponse response = testAdapter.getWithAuth("/api/notifications", token);
        sharedContext.setLastResponse(response);
    }

    @만일("해당 알림 읽음 처리 API를 호출한다")
    public void 해당_알림_읽음_처리_API를_호출한다() {
        String token = sharedContext.getAccessToken();
        TestResponse response = testAdapter.putWithAuth(
                "/api/notifications/" + testNotification.getId() + "/read",
                null,
                token
        );
        sharedContext.setLastResponse(response);
    }

    @만일("읽지 않은 알림 개수 조회 API를 호출한다")
    public void 읽지_않은_알림_개수_조회_API를_호출한다() {
        String token = sharedContext.getAccessToken();
        TestResponse response = testAdapter.getWithAuth("/api/notifications/unread-count", token);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 알림이 {int}개 있다")
    public void 응답에_알림이_N개_있다(int count) {
        TestResponse response = sharedContext.getLastResponse();
        int size = response.getJsonArraySize();
        assertThat(size).isEqualTo(count);
    }

    @그리고("해당 알림은 읽음 상태이다")
    public void 해당_알림은_읽음_상태이다() {
        Notification updated = notificationRepository.findById(testNotification.getId())
                .orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }

    @그리고("읽지 않은 알림 개수는 {int} 이다")
    public void 읽지_않은_알림_개수는_N_이다(int count) {
        TestResponse response = sharedContext.getLastResponse();
        int unreadCount = (int) response.getJsonValue("count");
        assertThat(unreadCount).isEqualTo(count);
    }
}
