package com.hoops.match.domain.repository;

import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Match 도메인 Repository 인터페이스 (DDD)
 *
 * 도메인 계층에 위치하여 영속성 추상화를 제공합니다.
 */
public interface MatchRepository {
    Match save(Match match);

    Optional<Match> findById(Long id);

    /**
     * 비관적 락을 사용하여 경기 조회
     * 참가자 수 변경 시 Race Condition 방지용
     */
    Optional<Match> findByIdWithLock(Long id);

    List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance);

    List<Match> findMatchesToStart(LocalDate date, LocalTime time, List<MatchStatus> statuses);

    List<Match> findMatchesToEnd(LocalDate date, LocalTime time, MatchStatus status);

    List<Match> findByHostId(Long hostId);

    List<Match> findActiveMatchesByHostId(Long hostId);

    List<Match> findAllByIds(List<Long> ids);
}
