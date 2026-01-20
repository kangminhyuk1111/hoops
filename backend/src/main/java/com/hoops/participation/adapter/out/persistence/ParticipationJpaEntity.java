package com.hoops.participation.adapter.out.persistence;

import com.hoops.common.infrastructure.persistence.BaseTimeEntity;
import com.hoops.participation.domain.vo.ParticipationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "participations", uniqueConstraints = {
        @UniqueConstraint(name = "idx_participation_unique", columnNames = {"match_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipationJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ParticipationStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public ParticipationJpaEntity(Long matchId, Long userId) {
        this(matchId, userId, ParticipationStatus.PENDING, LocalDateTime.now());
    }

    public ParticipationJpaEntity(Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public ParticipationJpaEntity(Long id, Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.id = id;
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public ParticipationJpaEntity(Long id, Long version, Long matchId, Long userId, ParticipationStatus status,
            LocalDateTime joinedAt) {
        this.id = id;
        this.version = version;
        this.matchId = matchId;
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
    }
}
