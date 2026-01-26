package com.hoops.common.event;

import java.time.LocalDateTime;

/**
 * 참가 관련 이벤트의 베이스 클래스
 */
public abstract class ParticipationEvent {

    private final Long participationId;
    private final Long matchId;
    private final Long userId;
    private final String matchTitle;
    private final LocalDateTime occurredAt;

    protected ParticipationEvent() {
        this.participationId = null;
        this.matchId = null;
        this.userId = null;
        this.matchTitle = null;
        this.occurredAt = null;
    }

    protected ParticipationEvent(Long participationId, Long matchId, Long userId,
                                  String matchTitle, LocalDateTime occurredAt) {
        this.participationId = participationId;
        this.matchId = matchId;
        this.userId = userId;
        this.matchTitle = matchTitle;
        this.occurredAt = occurredAt;
    }

    public Long getParticipationId() {
        return participationId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMatchTitle() {
        return matchTitle;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public abstract String getEventType();
}
