package com.hoops.common.event;

/**
 * 경기 참가 신청 완료 이벤트
 */
public class ParticipationCreatedEvent extends ParticipationEvent {

    public static final String EVENT_TYPE = "PARTICIPATION_CREATED";

    public ParticipationCreatedEvent() {
        super();
    }

    public ParticipationCreatedEvent(Long participationId, Long matchId, Long userId, String matchTitle) {
        super(participationId, matchId, userId, matchTitle);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}
