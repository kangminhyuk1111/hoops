package com.hoops.match.application.service;

import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import java.math.BigDecimal;
import java.util.List;

import com.hoops.match.application.exception.MatchNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchFinder implements MatchQueryUseCase {

    private final MatchRepository matchRepository;

    public MatchFinder(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    @Override
    public List<Match> loadMatchesByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        return matchRepository.findAllByLocation(latitude, longitude, distance);
    }
}
