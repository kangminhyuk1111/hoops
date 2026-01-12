package com.hoops.acceptance.mock;

import com.hoops.common.event.ParticipationEvent;
import com.hoops.participation.application.port.out.ParticipationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 Mock 이벤트 퍼블리셔
 * 실제 Kafka 대신 메모리에 이벤트를 저장합니다.
 */
@Component
@Profile("test")
@Primary
public class MockParticipationEventPublisher implements ParticipationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MockParticipationEventPublisher.class);

    private final List<ParticipationEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(ParticipationEvent event) {
        publishedEvents.add(event);
        log.info("[MOCK] 이벤트 발행: {} - matchId: {}, userId: {}",
                event.getEventType(), event.getMatchId(), event.getUserId());
    }

    public List<ParticipationEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }

    public void clear() {
        publishedEvents.clear();
    }

    public int getEventCount() {
        return publishedEvents.size();
    }
}
