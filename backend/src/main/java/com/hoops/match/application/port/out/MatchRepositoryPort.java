package com.hoops.match.application.port.out;

import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Match 영속성 포트 인터페이스
 */
public interface MatchRepositoryPort {
    Match save(Match match);

    Optional<Match> findById(Long id);

    /**
     * 비관적 락을 사용하여 경기 조회
     * 참가자 수 변경 시 Race Condition 방지용
     */
    Optional<Match> findByIdWithLock(Long id);

    List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance, int page, int size);

    List<Match> findMatchesToStart(LocalDate date, LocalTime time, List<MatchStatus> statuses);

    List<Match> findMatchesToEnd(LocalDate date, LocalTime time, MatchStatus status);

    List<Match> findByHostId(Long hostId);

    List<Match> findActiveMatchesByHostId(Long hostId);

    List<Match> findAllByIds(List<Long> ids);

    /**
     * 검색 대상 매치 조회 (PENDING, CONFIRMED 상태)
     */
    List<Match> findAllSearchableMatches();

    /**
     * 검색 대상 매치 ID만 조회 (메모리 효율적)
     * Geo Sync 스케줄러에서 사용
     */
    List<Long> findSearchableMatchIds();
}
