package com.hoops.participation.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Participation {

    private final Long id;
    private final Long matchId;
    private final Long userId;
    private final ParticipationStatus status;
    private final LocalDateTime joinedAt;

    public Participation(Long id, Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.id = id;
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public Participation cancel() {
        return new Participation(
                this.id,
                this.matchId,
                this.userId,
                ParticipationStatus.CANCELLED,
                this.joinedAt
        );
    }

    public boolean canCancel() {
        return this.status == ParticipationStatus.CONFIRMED;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }
}
