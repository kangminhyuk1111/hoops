package com.hoops.match.application.port.out;

import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    Match save(Match match);

    Optional<Match> findById(Long id);

    List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance);

    List<Match> findMatchesToStart(LocalDate date, LocalTime time, List<MatchStatus> statuses);

    List<Match> findMatchesToEnd(LocalDate date, LocalTime time, MatchStatus status);

    List<Match> findByHostId(Long hostId);

    List<Match> findActiveMatchesByHostId(Long hostId);

    List<Match> findAllByIds(List<Long> ids);
}
