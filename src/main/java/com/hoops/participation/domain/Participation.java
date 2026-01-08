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

    // Domain Logic - None strictly required yet as per YAGNI

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
