package com.hoops.participation.domain.repository;

import com.hoops.participation.domain.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository {
    Participation save(Participation participation);

    Optional<Participation> findById(Long id);

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    /**
     * 활성 상태(PENDING, CONFIRMED)의 참가 기록이 존재하는지 확인합니다.
     * 취소(CANCELLED), 거절(REJECTED), 경기취소(MATCH_CANCELLED) 상태는 제외합니다.
     */
    boolean existsActiveParticipation(Long matchId, Long userId);

    List<Participation> findByUserIdAndNotCancelled(Long userId);

    /**
     * 활성 상태(PENDING, CONFIRMED)의 참가 기록을 조회합니다.
     * 시간 중복 체크에 사용됩니다.
     */
    List<Participation> findActiveParticipationsByUserId(Long userId);

    List<Participation> findByMatchIdAndNotCancelled(Long matchId);

    /**
     * 취소 상태인 참가 기록을 조회합니다.
     * 재참가 시 기존 취소된 기록을 재활성화하기 위해 사용됩니다.
     */
    Optional<Participation> findCancelledParticipation(Long matchId, Long userId);
}
