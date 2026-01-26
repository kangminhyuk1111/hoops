package com.hoops.common.event;

import java.time.LocalDateTime;

/**
 * 경기 참가 취소 이벤트
 */
public class ParticipationCancelledEvent extends ParticipationEvent {

    public static final String EVENT_TYPE = "PARTICIPATION_CANCELLED";

    public ParticipationCancelledEvent() {
        super();
    }

    public ParticipationCancelledEvent(Long participationId, Long matchId, Long userId,
                                       String matchTitle, LocalDateTime occurredAt) {
        super(participationId, matchId, userId, matchTitle, occurredAt);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}
