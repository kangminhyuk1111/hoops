package com.hoops.match.application.service;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchFinder implements MatchQueryUseCase {

    private final MatchRepository matchRepository;

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    @Override
    public List<Match> loadMatchesByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        return matchRepository.findAllByLocation(latitude, longitude, distance);
    }

    @Override
    public List<Match> findMyHostedMatches(Long hostId) {
        return matchRepository.findByHostId(hostId);
    }
}
