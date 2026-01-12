package com.hoops.common.event;

/**
 * 경기 참가 취소 이벤트
 */
public class ParticipationCancelledEvent extends ParticipationEvent {

    public static final String EVENT_TYPE = "PARTICIPATION_CANCELLED";

    public ParticipationCancelledEvent() {
        super();
    }

    public ParticipationCancelledEvent(Long participationId, Long matchId, Long userId, String matchTitle) {
        super(participationId, matchId, userId, matchTitle);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}
