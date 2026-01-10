package com.hoops.participation.domain;

import java.time.LocalDateTime;

public class Participation {

    private Long id;
    private Long matchId;
    private Long userId;
    private ParticipationStatus status;
    private LocalDateTime joinedAt;

    public Participation(Long id, Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.id = id;
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    // Domain Logic

    /**
     * 참가를 취소합니다.
     *
     * @return CANCELLED 상태로 변경된 새 Participation 객체
     */
    public Participation cancel() {
        return new Participation(
                this.id,
                this.matchId,
                this.userId,
                ParticipationStatus.CANCELLED,
                this.joinedAt
        );
    }

    /**
     * 취소 가능한 상태인지 확인합니다.
     *
     * @return CONFIRMED 상태이면 true, 아니면 false
     */
    public boolean canCancel() {
        return this.status == ParticipationStatus.CONFIRMED;
    }

    /**
     * 해당 사용자가 이 참가의 소유자인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 소유자이면 true, 아니면 false
     */
    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getUserId() {
        return userId;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
