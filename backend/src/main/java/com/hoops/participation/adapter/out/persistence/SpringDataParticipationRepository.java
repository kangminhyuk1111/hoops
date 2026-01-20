package com.hoops.participation.adapter.out.persistence;

import com.hoops.participation.domain.vo.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataParticipationRepository extends JpaRepository<ParticipationJpaEntity, Long> {

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    boolean existsByMatchIdAndUserIdAndStatusIn(Long matchId, Long userId, List<ParticipationStatus> statuses);

    List<ParticipationJpaEntity> findByUserIdAndStatusNot(Long userId, ParticipationStatus status);

    List<ParticipationJpaEntity> findByUserIdAndStatusIn(Long userId, List<ParticipationStatus> statuses);

    List<ParticipationJpaEntity> findByMatchIdAndStatusNot(Long matchId, ParticipationStatus status);

    Optional<ParticipationJpaEntity> findByMatchIdAndUserIdAndStatus(Long matchId, Long userId, ParticipationStatus status);
}
