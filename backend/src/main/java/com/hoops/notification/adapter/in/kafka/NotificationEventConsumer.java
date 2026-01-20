package com.hoops.notification.adapter.in.kafka;

import com.hoops.common.event.ParticipationCancelledEvent;
import com.hoops.common.event.ParticipationCreatedEvent;
import com.hoops.common.event.ParticipationEvent;
import com.hoops.notification.application.port.in.CreateNotificationUseCase;
import com.hoops.notification.domain.vo.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 참가 이벤트를 수신하여 알림을 생성하는 Kafka Consumer
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final CreateNotificationUseCase createNotificationUseCase;

    public NotificationEventConsumer(CreateNotificationUseCase createNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @KafkaListener(
            topics = "participation-events",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleParticipationEvent(ParticipationEvent event) {
        log.info("이벤트 수신: {} - matchId: {}, userId: {}",
                event.getEventType(), event.getMatchId(), event.getUserId());

        try {
            if (event instanceof ParticipationCreatedEvent) {
                handleParticipationCreated((ParticipationCreatedEvent) event);
            } else if (event instanceof ParticipationCancelledEvent) {
                handleParticipationCancelled((ParticipationCancelledEvent) event);
            } else {
                log.warn("알 수 없는 이벤트 타입: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("이벤트 처리 실패: {} - {}", event.getEventType(), e.getMessage(), e);
        }
    }

    private void handleParticipationCreated(ParticipationCreatedEvent event) {
        String title = "참가 신청 완료";
        String message = String.format("'%s' 경기에 참가 신청이 완료되었습니다.", event.getMatchTitle());

        createNotificationUseCase.createNotification(
                event.getUserId(),
                NotificationType.PARTICIPATION_CREATED,
                title,
                message,
                event.getMatchId()
        );
    }

    private void handleParticipationCancelled(ParticipationCancelledEvent event) {
        String title = "참가 취소 완료";
        String message = String.format("'%s' 경기 참가가 취소되었습니다.", event.getMatchTitle());

        createNotificationUseCase.createNotification(
                event.getUserId(),
                NotificationType.PARTICIPATION_CANCELLED,
                title,
                message,
                event.getMatchId()
        );
    }
}
