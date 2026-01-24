package com.hoops.match.application.port.in;

import com.hoops.match.domain.model.Match;
import java.math.BigDecimal;
import java.util.List;

public interface MatchQueryUseCase {

    Match findMatchById(Long matchId);

    List<Match> loadMatchesByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance, int page, int size);

    List<Match> findMyHostedMatches(Long hostId);
}
