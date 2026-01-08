package com.hoops.participation.infrastructure;

import com.hoops.common.domain.BaseTimeEntity;
import com.hoops.participation.domain.ParticipationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "participations", uniqueConstraints = {
        @UniqueConstraint(name = "idx_participation_unique", columnNames = { "match_id",
                "user_id" })
})
public class ParticipationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ParticipationStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    protected ParticipationEntity() {
    }

    public ParticipationEntity(Long matchId, Long userId) {
        this(matchId, userId, ParticipationStatus.PENDING, LocalDateTime.now());
    }

    public ParticipationEntity(Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
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
