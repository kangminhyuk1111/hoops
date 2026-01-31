package com.hoops.match.application.port.in;

import com.hoops.match.application.dto.MatchLocationQueryResult;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchSortType;
import com.hoops.match.domain.vo.MatchStatus;

import java.math.BigDecimal;
import java.util.List;

public interface MatchQueryUseCase {

    Match getMatchById(Long matchId);

    MatchLocationQueryResult getMatchesByLocation(BigDecimal latitude, BigDecimal longitude, Double radiusKm,
                                                   int page, int size, MatchStatus status, MatchSortType sortType);

    List<Match> getMyHostedMatches(Long hostId);
}
