package com.hoops.participation.domain.model;

import com.hoops.participation.domain.exception.InvalidParticipationCreationException;
import com.hoops.participation.domain.exception.InvalidParticipationStateException;
import com.hoops.participation.domain.vo.ParticipationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Participation {

    private final Long id;
    private final Long version;
    private final Long matchId;
    private final Long userId;
    private final ParticipationStatus status;
    private final LocalDateTime joinedAt;

    private Participation(Long id, Long version, Long matchId, Long userId,
                          ParticipationStatus status, LocalDateTime joinedAt) {
        this.id = id;
        this.version = version;
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    /**
     * 새로운 참가 신청을 생성한다.
     * 도메인 불변식을 검증한다.
     */
    public static Participation createPending(Long matchId, Long userId) {
        validateCreation(matchId, userId);
        return new Participation(null, null, matchId, userId,
                ParticipationStatus.PENDING, LocalDateTime.now());
    }

    /**
     * 데이터베이스에서 복원할 때 사용한다.
     * 이미 검증된 데이터이므로 검증을 생략한다.
     */
    public static Participation reconstitute(Long id, Long version, Long matchId, Long userId,
                                              ParticipationStatus status, LocalDateTime joinedAt) {
        return new Participation(id, version, matchId, userId, status, joinedAt);
    }

    /**
     * 참가를 취소한다.
     * PENDING 또는 CONFIRMED 상태에서만 취소할 수 있다.
     */
    public Participation cancel() {
        validateCanCancel();
        return new Participation(this.id, this.version, this.matchId, this.userId,
                ParticipationStatus.CANCELLED, this.joinedAt);
    }

    /**
     * 참가 신청을 다시 활성화한다.
     * CANCELLED 상태에서만 재활성화할 수 있다.
     */
    public Participation reactivate() {
        validateCanReactivate();
        return new Participation(this.id, this.version, this.matchId, this.userId,
                ParticipationStatus.PENDING, LocalDateTime.now());
    }

    /**
     * 참가 신청을 승인한다.
     * PENDING 상태에서만 승인할 수 있다.
     */
    public Participation approve() {
        validateCanApproveOrReject();
        return new Participation(this.id, this.version, this.matchId, this.userId,
                ParticipationStatus.CONFIRMED, this.joinedAt);
    }

    /**
     * 참가 신청을 거절한다.
     * PENDING 상태에서만 거절할 수 있다.
     */
    public Participation reject() {
        validateCanApproveOrReject();
        return new Participation(this.id, this.version, this.matchId, this.userId,
                ParticipationStatus.REJECTED, this.joinedAt);
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

    private static void validateCreation(Long matchId, Long userId) {
        if (matchId == null) {
            throw new InvalidParticipationCreationException("matchId is required");
        }
        if (userId == null) {
            throw new InvalidParticipationCreationException("userId is required");
        }
    }

    private void validateCanCancel() {
        if (!canCancel()) {
            throw new InvalidParticipationStateException(this.status.name(), "cancel");
        }
    }

    private void validateCanReactivate() {
        if (this.status != ParticipationStatus.CANCELLED) {
            throw new InvalidParticipationStateException(this.status.name(), "reactivate");
        }
    }

    private void validateCanApproveOrReject() {
        if (!canBeApprovedOrRejected()) {
            throw new InvalidParticipationStateException(this.status.name(), "approve/reject");
        }
    }
}
