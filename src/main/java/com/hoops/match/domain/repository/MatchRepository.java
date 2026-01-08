package com.hoops.match.domain.repository;

import com.hoops.match.domain.Match;
import java.util.Optional;

public interface MatchRepository {
    Match save(Match match);

    Optional<Match> findById(Long id);
}
