package com.hoops.match.application.port.out;

import com.hoops.match.domain.Match;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    Match save(Match match);

    Optional<Match> findById(Long id);

    List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance);
}
