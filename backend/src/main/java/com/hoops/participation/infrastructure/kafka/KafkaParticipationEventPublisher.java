package com.hoops.participation.infrastructure.kafka;

import com.hoops.common.event.ParticipationEvent;
import com.hoops.participation.application.port.out.ParticipationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka를 통한 참가 이벤트 발행 어댑터
 */
@Component
public class KafkaParticipationEventPublisher implements ParticipationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaParticipationEventPublisher.class);
    private static final String TOPIC = "participation-events";

    private final KafkaTemplate<String, ParticipationEvent> kafkaTemplate;

    public KafkaParticipationEventPublisher(KafkaTemplate<String, ParticipationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(ParticipationEvent event) {
        String key = String.valueOf(event.getMatchId());

        kafkaTemplate.send(TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("이벤트 발행 실패: {} - {}", event.getEventType(), ex.getMessage());
                    } else {
                        log.info("이벤트 발행 성공: {} - matchId: {}, userId: {}",
                                event.getEventType(),
                                event.getMatchId(),
                                event.getUserId());
                    }
                });
    }
}
