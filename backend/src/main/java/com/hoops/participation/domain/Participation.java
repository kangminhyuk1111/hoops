package com.hoops.participation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Participation {

    private final Long id;
    private final Long version;
    private final Long matchId;
    private final Long userId;
    private final ParticipationStatus status;
    private final LocalDateTime joinedAt;

    public Participation cancel() {
        return Participation.builder()
                .id(this.id)
                .version(this.version)
                .matchId(this.matchId)
                .userId(this.userId)
                .status(ParticipationStatus.CANCELLED)
                .joinedAt(this.joinedAt)
                .build();
    }

    public Participation reactivate() {
        return Participation.builder()
                .id(this.id)
                .version(this.version)
                .matchId(this.matchId)
                .userId(this.userId)
                .status(ParticipationStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public Participation approve() {
        return Participation.builder()
                .id(this.id)
                .version(this.version)
                .matchId(this.matchId)
                .userId(this.userId)
                .status(ParticipationStatus.CONFIRMED)
                .joinedAt(this.joinedAt)
                .build();
    }

    public Participation reject() {
        return Participation.builder()
                .id(this.id)
                .version(this.version)
                .matchId(this.matchId)
                .userId(this.userId)
                .status(ParticipationStatus.REJECTED)
                .joinedAt(this.joinedAt)
                .build();
    }

    public boolean canCancel() {
        return this.status == ParticipationStatus.PENDING || this.status == ParticipationStatus.CONFIRMED;
    }

    public boolean canBeApprovedOrRejected() {
        return this.status == ParticipationStatus.PENDING;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }
}
